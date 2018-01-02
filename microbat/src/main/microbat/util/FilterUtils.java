package microbat.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.PackageFragment;

import sav.common.core.SavRtException;
import sav.common.core.utils.CollectionUtils;
import sav.common.core.utils.FileUtils;
import sav.common.core.utils.StringUtils;

public class FilterUtils {
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
	
	public static String[] deriveLibExcludePatterns(String[] libExcludes, String[] libIncludes) {
		IJavaProject ijavaProject = JavaCore.create(JavaUtil.getSpecificJavaProjectInWorkspace());
		return deriveLibExcludePatterns(new IJavaProjectPkgContainer(ijavaProject), libExcludes, libIncludes);
	}
	
	public static String[] deriveLibExcludePatternsUnderRtJar(String[] libExcludes, String[] libIncludes) {
		return deriveLibExcludePatterns(new ExtJarPackagesContainer(MicroBatUtil.getRtJarPathInDefinedJavaHome()),
				libExcludes, libIncludes);
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
	public static <T> String[] deriveLibExcludePatterns(PackagesContainer<T> pkgsContainer, String[] libExcludes, String[] libIncludes) {
		Set<String> newExcludeCol = CollectionUtils.toHashSet(libExcludes);
		for (String incl : libIncludes) {
			Set<String> expandedExclSet = new HashSet<String>();
			for (Iterator<String> it = newExcludeCol.iterator(); it.hasNext();) {
				String excl = it.next();
				if (excl.equals(incl)) {
					it.remove();
					break;
				}
				if (FilterUtils.isSubFilter(incl, excl)) {
					it.remove();
					expandPkgFilter(pkgsContainer, excl, incl, expandedExclSet);
				}
			}
			newExcludeCol.addAll(expandedExclSet);
		}
		return StringUtils.sortAlphanumericStrings(new ArrayList<>(newExcludeCol)).toArray(new String[0]);
	}
	
	/**
	 *  java.util.* : include all types and packages under java.util package
	 *  java.util.*\ : include all types only under java.util package
	 *  java.util.Arrays : include type Arrays only
	 *  java.util.Arrays\ : include type Arrays and its inner types
	 */
	private static <T>void expandPkgFilter(PackagesContainer<T> container, String pkgFilter, String incl,
			Set<String> expandedExclSet) {
		try {
			String pkgName = FilterUtils.getPrefix(pkgFilter);
			/* find packages match pkgFilter */
			System.out.println();
			List<T> matches = container.getMatchPkgs(pkgName);
			/* get include package */
			String[] inclFrags;
			String inclTypeSimpleName = null;
			String subTypePrefix = null;
			int typeSimpleNameSIdx = incl.lastIndexOf(".");
			inclFrags = StringUtils.dotSplit(incl.substring(0, typeSimpleNameSIdx));
			if (incl.endsWith("*\\")) {
				inclTypeSimpleName = "";
			} else if (!incl.endsWith("*")) {
				if (!incl.endsWith("\\")) {
					inclTypeSimpleName = incl.substring(typeSimpleNameSIdx + 1);
					subTypePrefix = inclTypeSimpleName + "$";
				} else {
					inclTypeSimpleName = incl.substring(typeSimpleNameSIdx + 1, incl.length() - 1);
				}
			} 
			/* build up exclude list */
			for (T pkg : matches) {
				/* add exclude packages */
				for (T otherPkg : container.getAllPkgsUnderPkgRoot(pkg)) {
					String[] otherPkgFrags = container.getPkgFragments(otherPkg);
					int i = 0;
					for (i = 0; i < inclFrags.length && i < otherPkgFrags.length; i++) {
						if (!inclFrags[i].equals(otherPkgFrags[i])) {
							break;
						}
					}
					if (i == otherPkgFrags.length) {
						/* ignore include package's parent */
						continue;
					}
					if (i < inclFrags.length) {
						expandedExclSet.add(FilterUtils.toPkgFilterText(otherPkgFrags, 0, i));
					} else if (i == inclFrags.length && i < otherPkgFrags.length  && inclTypeSimpleName != null) {
						/* add sub-package of include package */
						expandedExclSet.add(FilterUtils.toPkgFilterText(otherPkgFrags, 0, i));
					} 
				}
				/* add exclude types */
				if (inclTypeSimpleName != null) {
					for (String type : container.getTypeUnderPkg(pkg)) {
						String typeSimpleName = type.replace(".class", "");
						if (inclTypeSimpleName.isEmpty() || inclTypeSimpleName.equals(typeSimpleName)
								|| (subTypePrefix != null && typeSimpleName.startsWith(subTypePrefix))) {
							continue;
						}
						expandedExclSet.add(StringUtils.dotJoin(pkgName, typeSimpleName));
					}
				}
			}
		} catch (JavaModelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private static class IJavaProjectPkgContainer implements PackagesContainer<IPackageFragment> {
		private IJavaProject project;

		public IJavaProjectPkgContainer(IJavaProject ijavaProject) {
			this.project = ijavaProject;
		}

		@Override
		public List<IPackageFragment> getMatchPkgs(String pkgName) throws JavaModelException {
			List<IPackageFragment> matches = new ArrayList<>();
			for (IPackageFragmentRoot pkgRoot : project.getAllPackageFragmentRoots()) {
				IPackageFragment pkg = pkgRoot.getPackageFragment(pkgName);
				if (pkg.exists()) {
					matches.add(pkg);
				}
			}
			return matches;
		}

		@Override
		public Collection<IPackageFragment> getAllPkgsUnderPkgRoot(IPackageFragment pkg) throws JavaModelException {
			return Arrays.asList((IPackageFragment[])((IPackageFragmentRoot) pkg.getParent()).getChildren());
		}

		@Override
		public String[] getPkgFragments(IPackageFragment otherPkg) {
			return ((PackageFragment) otherPkg).names;
		}

		@Override
		public List<String> getTypeUnderPkg(IPackageFragment pkg) throws JavaModelException {
			List<String> types = new ArrayList<>();
			for (IJavaElement child : pkg.getChildren()) {
				if (child.getElementType() == IJavaElement.CLASS_FILE) {
					types.add(child.getElementName());
				}
			}
			return types;
		}
	}
	
	private static class ExtJarPackagesContainer implements PackagesContainer<String> {
		private Map<String, List<String>> pkgTypesMap = new HashMap<String, List<String>>();
		private List<String> allPkgPaths = new ArrayList<>();
		
		public ExtJarPackagesContainer(String jarPath) {
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
						allPkgPaths.add(entryName);
					} else {
						int typeNameStartIdx = entryName.lastIndexOf("/");
						String pkgPath = entryName.substring(0, typeNameStartIdx);
						if (entryName.endsWith(".class")) {
							List<String> classes = pkgTypesMap.get(pkgPath);
							if (classes == null) {
								classes = new ArrayList<>();
								pkgTypesMap.put(pkgPath, classes);
								allPkgPaths.add(pkgPath);
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

		@Override
		public Collection<String> getAllPkgsUnderPkgRoot(String pkg) throws JavaModelException {
			return allPkgPaths;
		}

		@Override
		public List<String> getTypeUnderPkg(String pkg) throws JavaModelException {
			return CollectionUtils.nullToEmpty(pkgTypesMap.get(pkg));
		}

		@Override
		public String[] getPkgFragments(String otherPkg) {
			return otherPkg.split("/");
		}

		@Override
		public List<String> getMatchPkgs(String pkgName) throws JavaModelException {
			String pkgPath = pkgName.replace(".", "/");
			for (String pkg : allPkgPaths) {
				if (pkg.startsWith(pkgPath)) {
					return CollectionUtils.listOf(pkg);
				}
			}
			return Collections.EMPTY_LIST;
		}
	}
	
	private static interface PackagesContainer<T> {

		Collection<T> getAllPkgsUnderPkgRoot(T pkg) throws JavaModelException;

		List<T> getMatchPkgs(String pkgName) throws JavaModelException;

		List<String> getTypeUnderPkg(T pkg) throws JavaModelException;

		String[] getPkgFragments(T pkg);
		
	}
}
