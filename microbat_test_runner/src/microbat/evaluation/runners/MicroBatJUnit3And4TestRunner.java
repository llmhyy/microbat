package microbat.evaluation.runners;

import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class MicroBatJUnit3And4TestRunner extends TestRunner {
    @Override
    public void runTest(final String className, final String methodName) {
        Request request;
        try {
            request = Request.method(Class.forName(className), methodName);
            JUnitCore jUnitCore = new JUnitCore();
            jUnitCore.addListener(new RunListener() {
                @Override
                public void testStarted(Description description) throws Exception {
                    $testStarted(className, methodName);
                }

                @Override
                public void testFinished(Description description) throws Exception {
                    $testFinished(className, methodName);
                }
            });
            Result result = jUnitCore.run(request);
            setSuccessful(result.wasSuccessful());

            List<Failure> failures = result.getFailures();
            for (Failure failure : failures) {
                Throwable exception = failure.getException();
                this.failureMessage = exception.getMessage();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        System.currentTimeMillis();
        System.out.println("is successful? " + successful);
        System.out.println(this.failureMessage);
        $exitProgram(successful + ";" + this.failureMessage);
    }
}
