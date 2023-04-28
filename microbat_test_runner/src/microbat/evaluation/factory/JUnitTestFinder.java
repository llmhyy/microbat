package microbat.evaluation.factory;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

public class JUnitTestFinder {
    private boolean foundJUnit3Or4Test = false;

    public boolean junit3Or4TestExists(final String className, final String methodName) {
        // Create a RunNotifier to receive notifications about test runs
        RunNotifier notifier = new RunNotifier();

        // Create a RunListener to listen for test events
        RunListener listener = new RunListener() {
            @Override
            public void testStarted(Description description) throws Exception {
                String testDisplayName = description.getDisplayName();
                // If test is not found, its name is initializationError
                foundJUnit3Or4Test = !testDisplayName.startsWith("initializationError");
            }
        };

        // Register the RunListener with the RunNotifier
        notifier.addListener(listener);

        // Get a request for all the tests in the TestSuite class
        Request request;
        try {
            request = Request.method(Class.forName(className), methodName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        // Get the runner for the request
        Runner runner = request.getRunner();

        // Notify the RunNotifier that the tests are starting
        notifier.fireTestRunStarted(runner.getDescription());

        // Notify the Runner to run the tests
        runner.run(notifier);

        // Notify the RunNotifier that the tests are finished
        notifier.fireTestRunFinished(new Result());
        
        boolean result = foundJUnit3Or4Test;
        foundJUnit3Or4Test = false;
        return result;
    }

    public boolean junit5TestExists(String className, String methodName) {
        try {
            LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                    .selectors(DiscoverySelectors.selectMethod(className, methodName)).build();
            Launcher launcher = LauncherFactory.create();
            // Technically the Launcher can find JUnit 3 and 4 tests as well, however,
            // it requires JUnit 4.12 (or above) in the classpath, which may be overriden by
            // the project's junit lib.
            TestPlan testPlan = launcher.discover(request);
            if (testPlan.containsTests()) {
                return true;
            }
        } catch (JUnitException e) {
            // Not JUnit 5
        }
        return false;
    }
}
