package microbat.evaluation.factory;

import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

public class JUnit3And4TestFinder {
    private boolean foundTest = false;
    public boolean junit3Or4TestExists(final String className, final String methodName) {
        // Create a RunNotifier to receive notifications about test runs
        RunNotifier notifier = new RunNotifier();
    
        // Create a RunListener to listen for test events
        RunListener listener = new RunListener() {
            @Override
            public void testStarted(Description description) throws Exception {
                foundTest = true;
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
        return foundTest;
    }
}
