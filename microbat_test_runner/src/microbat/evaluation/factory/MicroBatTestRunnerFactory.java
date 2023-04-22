package microbat.evaluation.factory;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import microbat.evaluation.runners.MicroBatJUnit3And4TestRunner;
import microbat.evaluation.runners.MicroBatJUnit5TestRunner;
import microbat.evaluation.runners.MicroBatTestNGTestRunner;
import microbat.evaluation.runners.TestRunner;

// Decides which test runner to use e.g. junit 4/5, junit 3, testng
public class MicroBatTestRunnerFactory {
    public TestRunner create(String className, String methodName) {
        try {
            LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                    .selectors(DiscoverySelectors.selectMethod(className, methodName)).build();
            Launcher launcher = LauncherFactory.create();
            // Technically the Launcher can find JUnit 3 and 4 tests as well, however, 
            // it requires JUnit 4.12 in the classpath, which may be overriden by the project's junit lib.
            TestPlan testPlan = launcher.discover(request); 
            if (testPlan.containsTests()) {
                return new MicroBatJUnit5TestRunner();
            }
        } catch (JUnitException e) {
            // Not JUnit 5
        }
        boolean isJUnit3Or4Test = new JUnit3And4TestFinder().junit3Or4TestExists(className, methodName);
        if (isJUnit3Or4Test) {
            return new MicroBatJUnit3And4TestRunner();
        }
        return new MicroBatTestNGTestRunner();
    }
}
