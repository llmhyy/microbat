package microbat.evaluation.factory;

import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import microbat.evaluation.runners.MicroBatJUnitTestRunner;
import microbat.evaluation.runners.MicroBatTestNGTestRunner;
import microbat.evaluation.runners.TestRunner;

// Decides which test runner to use e.g. junit, testng
public class MicroBatTestRunnerFactory {
    public TestRunner create(String className, String methodName) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectMethod(className, methodName)).build();
        Launcher launcher = LauncherFactory.create();
        TestPlan testPlan = launcher.discover(request);
        if (testPlan.containsTests()) {
            return new MicroBatJUnitTestRunner();
        }
        return new MicroBatTestNGTestRunner();
    }
}
