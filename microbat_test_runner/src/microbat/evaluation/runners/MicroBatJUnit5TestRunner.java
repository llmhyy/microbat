package microbat.evaluation.runners;

import java.util.List;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;

/**
 * Runner for JUnit 5.
 * This runner technically works for JUnit 3 and 4 as well, however, it requires JUnit 4.12 to be in the classpath.
 * Our JUnit 4.12 jar (junit.jar) may be overriden by the project's own junit library, which could be older than junit 4.12. 
 * For such cases, the other runner (MicroBatJUnit3And4TestRunner) should be used.
 * @author bchenghi
 *
 */
public class MicroBatJUnit5TestRunner extends TestRunner {
    public void runTest(final String className, final String methodName) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectMethod(className, methodName)).build();
        SummaryGeneratingListener listener = new SummaryGeneratingListener() {
            @Override
            public void executionStarted(TestIdentifier testIdentifier) {
                if (checkMethodNameMatch(testIdentifier.getDisplayName(), methodName)) {
                    super.executionStarted(testIdentifier);
                    $testStarted(className, methodName);
                }
            }

            @Override
            public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
                if (checkMethodNameMatch(testIdentifier.getDisplayName(), methodName)) {
                    $testFinished(className, methodName);
                    super.executionFinished(testIdentifier, testExecutionResult);
                }
            }
        };
        Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);
        TestExecutionSummary summary = listener.getSummary();
        setSuccessful(summary.getTotalFailureCount() == 0);

        List<TestExecutionSummary.Failure> failures = summary.getFailures();
        for (TestExecutionSummary.Failure failure : failures) {
            Throwable exception = failure.getException();
            this.failureMessage = exception.getMessage();
        }

        System.currentTimeMillis();
        System.out.println("is successful? " + successful);
        System.out.println(this.failureMessage);
        $exitProgram(successful + ";" + this.failureMessage);
    }

    private boolean checkMethodNameMatch(String junitDisplayName, String methodName) {
        // JUnit5's test names have brackets, e.g. test(),
        // while JUnit4 does not e.g. test
        boolean isJUnit5 = junitDisplayName.indexOf("(") != -1;
        if (isJUnit5) {
            return junitDisplayName.startsWith(methodName + "(");
        }
        return junitDisplayName.equals(methodName);
    }
}
