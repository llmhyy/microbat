# Microbat Debugger
A feedback-based debugger for interactively recommending suspicious step in buggy program execution.

![Snapshot of Microbat](/microbat/image/microbat_snapshot.jpg?raw=true "Snapshot of Microbat")

Microbat is a feedback-based debugger which aims to locate bug by interactively recommending suspicious program steps with developers' feedback. Given a buggy program, Microbat records its execution trace and allow developers to make light-weight feedback on trace steps, such as correct-step, wrong-variable-value, wrong-path, and unclear. Microbat reasons and analyzes the feedback along with program information to recommend a suspicious step for further inspection and feedback. Such a debugging process continues until the bug is found. A short demonstration of Microbat is available in http://linyun.info/microbat/index.html.

# Feedback Type
We support four types of feedback, i.e., correct-step, wrong-variable-value, wrong-path, and unclear. Based on these types of feedback, we iteratively and interactively recommend suspicious steps on recoreded trace.

# Evaluation
Our evaluation on Microbat shows promising results. The __detailed evaluation results__ can be checked at http://linyun.info/microbat/index.html

# Citation
If you need to reference our technique, please use the following citations:

Yun Lin, Jun Sun, Yinxing Xue, Yang Liu, and Jinsong Dong. Feedback-based Debugging. The 39th ACM SIGSOFT International Conference on Software Engineering (ICSE 2017), pp. 393-403.

# Installation
Our debugger can be divided into two parts: trace collector and the bug inference engine. Two parts are presented in terms of an Eclipse plugin. 
- After you clone the git repository, you need to load the microbat repository into __eclipse__ (as the tool is manifested by Eclipse plugin). We recommend that the user should import the project through "Git perspective".
<p align="center">
  <img src="/microbat/image/f1.png" width="300">
</p>

- Given our trace collector is implemented through Java instrumentation technique, the user need to run microbat.tools.JarPackageTool. Please modify the DEPLOY_DIR by your eclipse_root_folder\dropins\junit_lib\. After running the code, you will generate an instrumentator.jar under the DEPLOY_DIR folder.
- Remeber to replace the instrumentaor.jar in the lib folder under microbat project.
<p align="center">
  <img src="/microbat/image/f2.png" width="300" align="center">
</p>
- You may run the code as an Eclipse Application then.
- In the running Eclipse Application, specify the configuration in Perspective>>Microbat Debugging. Here, you need to specify (1) which eclipse project you are going to debug; (2) where is the JDK library for running your Java program; (3) Step Limit (e.g., 10000); (4) Variable Layer (e.g., 2); and (5) Lanuch Class. Lanuch class is supposed to be the class containing main() method. Alternatively, you can also speicify a Junit test case.
- Then, you can click the Microbat (in Eclipse Menu) >> Start Debug. The hierarhical trace will be generated and you can provide the feedback for debuggng then.



