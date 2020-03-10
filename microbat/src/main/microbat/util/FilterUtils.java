package microbat.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sav.common.core.SavRtException;
import sav.common.core.utils.CollectionUtils;
import sav.common.core.utils.StringUtils;

public class FilterUtils {
	private static final Logger log = LoggerFactory.getLogger(FilterUtils.class);
	private FilterUtils(){}
	private static final String SUFFIX = ".*";
	
	/* pattern: text.*   */
	public static String getPrefix(String filterText) {
		return filterText.replace(SUFFIX, "");
	}
	
	public static String toFilterText(String prefix) {
		return prefix + SUFFIX;
	}
	
	/**
	 * [from, to]
	 **/
	public static String toPkgFilterText(String[] pkgFrags, int from, int to) {
		StringBuilder sb = new StringBuilder();
		for (int i = from; i <= to; i++) {
			sb.append(pkgFrags[i]);
			if (i != to) {
				sb.append(".");
			}
		}
		sb.append(SUFFIX);
		return sb.toString();
	}
	
	/**
	 * s1 is subFilter of s2 when:
	 * s1 = a.b.c.* | a.b.c
	 * s2 = a.b.*
	 * */
	public static boolean isSubFilter(String s1, String s2) {
		int n1 = s1.length();
		int n2 = s2.length();
		if (n1 <= n2) {
			return false;
		}
		for (int i = 0; i < n2; i++) {
			char c1 = s1.charAt(i);
			char c2 = s2.charAt(i);
			if (c2 == '*') {
				return true;
			}
			if (c1 != c2) {
				return false;
			}
		}
		return false;
	}
	
	/**
	 * This method derive more detailed libExcludes with libIncludes, for example, 
	 * when libExcludes has a pattern as java.* while libIncludes has a pattern as java.util.*,
	 * then the method can split java.* into a list including java.awt.*, java.lang.*, ..., except
	 * java.util.*. 
	 * @param libExcludes 
	 * @param libIncludes 
	 *
	 * @return
	 */
	public static <T> Set<String> deriveLibExcludePatterns(PackagesContainer<T> pkgsContainer, String[] libExcludes,
			String[] libIncludes) {
		Set<String> newExcludeCol = CollectionUtils.toHashSet(libExcludes);
		newExcludeCol.addAll(pkgsContainer.getDefaultExcludes());
		for (String incl : libIncludes) {
			Set<String> expandedExclSet = new HashSet<String>();
			for (Iterator<String> it = newExcludeCol.iterator(); it.hasNext();) {
				String excl = it.next();
				if (excl.equals(incl)) {
					it.remove();
					break;
				}
				if (FilterUtils.isSubFilter(incl, excl)) {
					if (expandPkgFilter(pkgsContainer, excl, incl, expandedExclSet)) {
						it.remove();
					}
				}
			}
			newExcludeCol.addAll(expandedExclSet);
		}
		
		newExcludeCol = applyHeuristicRules(newExcludeCol);
		
		return newExcludeCol;
	}
	
	/**
	 * com.* and org.* are too popular package name, so we need to make it more specific.
	 * @param newExcludeCol
	 * @return
	 */
	private static Set<String> applyHeuristicRules(Set<String> newExcludeCol) {
		if(newExcludeCol.contains("com.*")){
			newExcludeCol.remove("com.*");
			newExcludeCol.add("com.oracle.*");
			newExcludeCol.add("com.sun.*");
		}
		
		if(newExcludeCol.contains("org.*")){
			newExcludeCol.remove("org.*");
			newExcludeCol.add("org.ietf.*");
			newExcludeCol.add("org.jcp.*");
			newExcludeCol.add("org.omg.*");
			newExcludeCol.add("org.w3c.*");
			newExcludeCol.add("org.xml.*");
		}
		
		return newExcludeCol;
	}

	/**
	 *  java.util.* : include all types and packages under java.util package
	 *  java.util.*\ : include all types only under java.util package
	 *  java.util.Arrays : include type Arrays only
	 *  java.util.Arrays* : include type Arrays and its inner types
	 */
	private static <T>boolean expandPkgFilter(PackagesContainer<T> container, String pkgFilter, String incl,
			Set<String> expandedExclSet) {
		try {
			String pkgName = FilterUtils.getPrefix(pkgFilter);
			/* find packages match pkgFilter */
			List<T> matches = container.getMatchPkgs(pkgName);
			if (matches.isEmpty()) {
				return false;
			}
			/* get include package */
			String[] inclFrags;
			String inclTypeSimpleName = null;
			String subTypePrefix = null;
			int typeSimpleNameSIdx = incl.lastIndexOf(".");
			inclFrags = StringUtils.dotSplit(incl.substring(0, typeSimpleNameSIdx));
			if (incl.endsWith("*\\")) {
				inclTypeSimpleName = "";
			} else if (!incl.endsWith(".*")) {
				if (incl.endsWith("*")) {
					inclTypeSimpleName = incl.substring(typeSimpleNameSIdx + 1, incl.length() - 1);
					subTypePrefix = inclTypeSimpleName + "$";
				} else {
					inclTypeSimpleName = incl.substring(typeSimpleNameSIdx + 1);
				}
			} 
			/* build up exclude list */
			for (T pkg : matches) {
				String[] matchPkgFrags = container.getPkgFragments(pkg);
				/* add exclude packages */
				for (T otherPkg : container.getSubPackages(pkg)) {
					String[] otherPkgFrags = container.getPkgFragments(otherPkg);
					int i = 0;
					for (i = 0; i < inclFrags.length && i < otherPkgFrags.length; i++) {
						if (!inclFrags[i].equals(otherPkgFrags[i])) {
							break;
						}
					}
					/* parent or incl package itself */
					if (i == otherPkgFrags.length) {
						String otherPkgName = StringUtils.dotJoin((Object[]) otherPkgFrags);
						/* parent */
						if (i < inclFrags.length) {
							// only expand types in case otherPkg is matchPkg (exclude pkg) or its sub package.
							if (otherPkgFrags.length >= matchPkgFrags.length) { 
								/* add all classes under include_package's parent to exclude list */
								for (String type : container.getTypeUnderPkg(otherPkg)) {
									String typeSimpleName = type.substring(0, type.lastIndexOf("."));
									expandedExclSet.add(StringUtils.dotJoin(otherPkgName, typeSimpleName));
								}
							}
						} else {
							/* same package */
							/* add exclude types */
							if (inclTypeSimpleName != null) {
								for (String type : container.getTypeUnderPkg(otherPkg)) {
									String typeSimpleName = type.substring(0, type.lastIndexOf("."));
									if (inclTypeSimpleName.isEmpty() || inclTypeSimpleName.equals(typeSimpleName)
											|| (subTypePrefix != null && typeSimpleName.startsWith(subTypePrefix))) {
										continue;
									}
									expandedExclSet.add(StringUtils.dotJoin(otherPkgName, typeSimpleName));
								}
							}
						}
						continue;
					}
					if (i < inclFrags.length) {
						expandedExclSet.add(FilterUtils.toPkgFilterText(otherPkgFrags, 0, i));
					} else if (i == inclFrags.length && i < otherPkgFrags.length  && inclTypeSimpleName != null) {
						/* add sub-package of include package */
						expandedExclSet.add(FilterUtils.toPkgFilterText(otherPkgFrags, 0, i));
					} 
				}
			}
		} catch (JavaModelException e1) {
			e1.printStackTrace();
		}
		return true;
	}
	
	public static class IJavaProjectPkgContainer implements PackagesContainer<IPackageFragment> {
		private IJavaProject project;

		public IJavaProjectPkgContainer(IJavaProject ijavaProject) {
			this.project = ijavaProject;
		}

		@Override
		public List<IPackageFragment> getMatchPkgs(String pkgName) throws JavaModelException {
			List<IPackageFragment> matches = new ArrayList<>();
			for (IClasspathEntry entry : project.getRawClasspath()) {
				if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					for (IPackageFragmentRoot pkgRoot : project.findPackageFragmentRoots(entry)) {
						IPackageFragment pkg = pkgRoot.getPackageFragment(pkgName);
						if (pkg.exists()) {
							matches.add(pkg);
						}
					}
				}
			}
			return matches;
		}

		@Override
		public Collection<IPackageFragment> getSubPackages(IPackageFragment pkg) throws JavaModelException {
			IJavaElement[] pkgRootChildren = ((IPackageFragmentRoot) pkg.getParent()).getChildren();
			List<IPackageFragment> pkgs = new ArrayList<>(pkgRootChildren.length);
			for (IJavaElement child : pkgRootChildren) {
				if (child.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
					if (child.getElementName().startsWith(pkg.getElementName())) {
						pkgs.add((IPackageFragment) child);
					}
				}
			}
			return pkgs;
		}

		@Override
		public String[] getPkgFragments(IPackageFragment otherPkg) {
			return StringUtils.dotSplit(otherPkg.getElementName());
		}

		@Override
		public List<String> getTypeUnderPkg(IPackageFragment pkg) throws JavaModelException {
			List<String> types = new ArrayList<>();
			for (IJavaElement child : pkg.getChildren()) {
				if (CollectionUtils.existIn(child.getElementType(), IJavaElement.CLASS_FILE,
						IJavaElement.COMPILATION_UNIT, IJavaElement.TYPE)) {
					types.add(child.getElementName());
				}
			}
			return types;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Set<String> getDefaultExcludes() {
			return Collections.EMPTY_SET;
		}
	}
	
	public static class ExtJarPackagesContainer implements PackagesContainer<String> {
		private Map<String, List<String>> pkgTypesMap = new HashMap<String, List<String>>(); // map of pkg and its types.
		private Map<String, Collection<String>> pkgRootsMap = new HashMap<>(); // map of pkgRoot and its packages.
		private boolean isCollectDefaultExcludes;
		
		public ExtJarPackagesContainer(String jarPath, boolean initDefaultExcludes) {
			this.isCollectDefaultExcludes = initDefaultExcludes;
			appendJar(jarPath);
		}
		
		public ExtJarPackagesContainer(List<String> jarPaths, boolean initDefaultExcludes) {
			pkgTypesMap.clear();
			pkgRootsMap.clear();
			this.isCollectDefaultExcludes = initDefaultExcludes;
			for (String jarPath : jarPaths) {
				appendJar(jarPath);
			}
		}

		private void appendJar(String jarPath) {
			JarFile jar = null;
			try {
				jar = new JarFile(jarPath);
				Enumeration<? extends JarEntry> enumeration = jar.entries();
				while (enumeration.hasMoreElements()) {
					JarEntry jarEntry = enumeration.nextElement();
					String entryName = jarEntry.getName();
					if (entryName.startsWith("META-INF")) {
						continue;
					}
					int idx = entryName.indexOf(".");
					if (idx < 0) {
						addPkgPath(entryName);
					} else {
						int typeNameStartIdx = entryName.lastIndexOf("/");
						if (typeNameStartIdx < 0) {
							System.out.println(String.format("ingore jar entry: %s in %s", entryName, jarPath));
							continue;
						}
						String pkgPath = entryName.substring(0, typeNameStartIdx);
						if (entryName.endsWith(".class")) {
							List<String> classes = pkgTypesMap.get(pkgPath);
							if (classes == null) {
								classes = new ArrayList<>();
								pkgTypesMap.put(pkgPath, classes);
								addPkgPath(pkgPath);
							}
							classes.add(entryName.substring(typeNameStartIdx + 1));
						}
					}
				}
			} catch(IOException ex) {
				throw new SavRtException(ex.getMessage());
			} finally {
				if (jar != null) {
					try {
						jar.close();
					} catch (IOException e) {
					}
				}
			}
		}
		
		private void addPkgPath(String entryName) {
			String rootPkg = getPkgRoot(entryName);
			if (entryName.endsWith("/")) {
				entryName = entryName.substring(0, entryName.length() - 1);
			}
			Collection<String> subPkgs = pkgRootsMap.get(rootPkg);
			if (subPkgs == null) {
				subPkgs = new HashSet<>();
				subPkgs.add(rootPkg);
				pkgRootsMap.put(rootPkg, subPkgs);
			}
			subPkgs.add(entryName);
		}

		private String getPkgRoot(String entryName) {
			int idx = entryName.indexOf("/");
			String rootPkg = entryName;
			if (idx >= 0) {
				rootPkg = entryName.substring(0, idx);
			}
			return rootPkg;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public Set<String> getDefaultExcludes() {
			if (!isCollectDefaultExcludes) {
				return Collections.EMPTY_SET;
			}
			Set<String> excludes = new HashSet<>();
			for (String pkgRoot : pkgRootsMap.keySet()) {
				excludes.add(FilterUtils.toFilterText(pkgRoot));
			}
			return excludes;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public Collection<String> getSubPackages(String pkgPath) throws JavaModelException {
			List<String> pkgs = getPkgsUnderSameRoot(pkgPath);
			int firstIdx = getMatchIdx(pkgs, pkgPath);
			if (firstIdx < 0) {
				return Collections.EMPTY_LIST;
			}
			List<String> subPkgs = new ArrayList<>();
			for (int i = firstIdx; i < pkgs.size(); i++) {
				String otherPkg = pkgs.get(i);
				if (!otherPkg.startsWith(pkgPath)) {
					break;
				}
				subPkgs.add(otherPkg);
			}
			return subPkgs;
		}

		@Override
		public List<String> getTypeUnderPkg(String pkg) throws JavaModelException {
			return CollectionUtils.nullToEmpty(pkgTypesMap.get(pkg));
		}

		@Override
		public String[] getPkgFragments(String otherPkg) {
			return otherPkg.split("/");
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<String> getMatchPkgs(String pkgName) throws JavaModelException {
			String pkgPath = pkgName.replace(".", "/");
			List<String> pkgsUnderSameRoot = getPkgsUnderSameRoot(pkgPath);
			if (pkgsUnderSameRoot == null) {
				return Collections.EMPTY_LIST;
			}
			if (pkgName.equals(pkgPath) /* pkg root */ || (getMatchIdx(pkgsUnderSameRoot, pkgPath) >= 0)) { 
				return CollectionUtils.listOf(pkgPath); 
			}
			return Collections.EMPTY_LIST;
		}
		
		/**
		 * @return exact idx of pkgPath in list,
		 * or in case pkgPath is not defined, idx of its first subPkgPath will be returned;
		 */
		private int getMatchIdx(List<String> pkgsUnderSameRoot, String pkgPath) {
			// idx = -(insertion point) - 1
			int idx = Collections.binarySearch(pkgsUnderSameRoot, pkgPath);
			if (idx >= 0) {
				return idx;
			}
			int insertPoint = -1 - idx; // insertion point idx
			if (insertPoint < pkgsUnderSameRoot.size() && pkgsUnderSameRoot.get(insertPoint).startsWith(pkgPath)) {
				return insertPoint;
			}
			return idx;
		}

		private List<String> getPkgsUnderSameRoot(String pkgPath) {
			String pkgRoot = getPkgRoot(pkgPath);
			Collection<String> pkgsUnderSameRoot = pkgRootsMap.get(pkgRoot);
			if (pkgsUnderSameRoot instanceof List) {
				return (List<String>) pkgsUnderSameRoot;
			}
			if (pkgsUnderSameRoot != null) {
				List<String> pkgList = new ArrayList<>(pkgsUnderSameRoot);
				Collections.sort(pkgList);
				pkgRootsMap.put(pkgRoot, pkgList);
				return pkgList;
			}
			return null;
		}
	}
	
	private static interface PackagesContainer<T> {

		Collection<T> getSubPackages(T pkg) throws JavaModelException;

		Set<String> getDefaultExcludes();

		List<T> getMatchPkgs(String pkgName) throws JavaModelException;

		List<String> getTypeUnderPkg(T pkg) throws JavaModelException;

		String[] getPkgFragments(T pkg);
		
	}
}
