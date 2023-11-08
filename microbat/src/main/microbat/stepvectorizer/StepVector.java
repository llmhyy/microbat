package microbat.stepvectorizer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A vector representation of a step or trace node in trace
 * @author Ding YuChen & David
 *
 */
public class StepVector {
		/**
		 * size of variables for this trace
		 */
		private int size;

		/**
		 * Trace order of this step
		 */
		public int order;
		
		/**
		 * True if the step is an "Throw Exception" step
		 */
		public boolean isThrowingException;

		/**
		 * True if the step is inside any for loop
		 */
		public boolean isInsideLoop;

		/**
		 * True if the step is inside any if statement
		 */
		public boolean isInsideIf;

		/**
		 * True if the step is a condition
		 */
		public boolean isCondition;

		/**
		 * True if there are any method called in the step
		 */
		public boolean haveMethodCalled;

		public Set<Integer> controlFlow;
		public Set<Integer> dataFlow;

		public int stepInPrev;
		public int stepOverPrev;
		public int stepInNext;
		public int stepOverNext;

		/**
		 * One-hot vector of read variables used in the step
		 */
		public Set<Integer> rVariable;

		/**
		 * One-hot vector of written variables used in the step
		 */
		public Set<Integer> wVariable;
		
		/**
		 * The maximum trace order in the trace that this step belongs to
		 */
		public int max_trace_order;
		
		/**
		 * Constructor set everything to false
		 * @param size Size of variables
		 */
		public StepVector(int size) {
			this.order = 0;
			this.size = size;
			this.isThrowingException = false;
			this.isInsideLoop = false;
			this.isInsideIf = false;
			this.isCondition = false;
			this.rVariable = new HashSet<>();
			this.wVariable = new HashSet<>();
			this.controlFlow = new HashSet<>();
			this.dataFlow = new HashSet<>();
			this.max_trace_order = 0;
		}

		public void setRead(int i) {
			if (i >= this.size) {
				throw new IllegalArgumentException("index exceeds variables length");
			}
			rVariable.add(i);
		}

		public void setWrite(int i) {
			if (i >= this.size) {
				throw new IllegalArgumentException("index exceeds variables length");
			}
			wVariable.add(i);
		}
		
		public void setMaxTraceOrder(int i) {
			this.max_trace_order = i;
		}

		public void setOrder(int i) {
			this.order = i;
		}
		
		public String convertToCSV() {
			return Stream.of(this.order, this.isThrowingException, this.isInsideLoop, this.isInsideIf, this.isCondition,
					this.stringifyIntSet(rVariable), this.stringifyIntSet(wVariable), this.stepOverNext, this.stepInNext,
					this.stepOverPrev, this.stepInPrev, this.stringifyIntSet(this.controlFlow),
					this.stringifyIntSet(this.dataFlow), this.max_trace_order).map(b -> b.toString()).collect(Collectors.joining(","));
		}

		private String stringifyIntSet(Set<Integer> set) {
			return "[" + set.stream().map(x -> x.toString()).collect(Collectors.joining(" ")) + "]";
		}

		private String stringifyBoolArray(boolean[] arr) {
			return Arrays.toString(arr).replace(",", "").replace("true", "1").replace("false", "0");
		}

		@Override
		public String toString() {
			String attributes = String.format("isException: %s; isInLoop: %s; isInConditional: %s; isCondition: %s;",
					this.isThrowingException, this.isInsideLoop, this.isInsideIf, this.isCondition);
			return attributes + rVariable.toString() + wVariable.toString();
		}
}
