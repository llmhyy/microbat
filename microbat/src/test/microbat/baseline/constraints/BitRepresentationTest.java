package microbat.baseline.constraints;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import microbat.probability.BP.constraint.BitRepresentation;

public class BitRepresentationTest {

	private BitRepresentation bitRepresentation;
	
	@Before
	public void init() {
		this.bitRepresentation = new BitRepresentation(3);
	}
	
	@Test
	public void testInit() {
		assertEquals(3, this.bitRepresentation.size());
		assertEquals(0, this.bitRepresentation.getCardinality());
		for (int idx=0; idx<3; idx++) {
			assertFalse(this.bitRepresentation.get(idx));
		}
	}
	
	@Test
	public void testSet() {
		this.bitRepresentation.set(1);
		assertFalse(this.bitRepresentation.get(0));
		assertTrue(this.bitRepresentation.get(1));
		assertFalse(this.bitRepresentation.get(2));
		assertEquals(1, this.bitRepresentation.getCardinality());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSet_IllegalArgumentException() {
		this.bitRepresentation.set(-1);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void testSet_IndexOutOfBoundsException() {
		this.bitRepresentation.set(3);
	}
	
	@Test
	public void testSetMultiple() {
		this.bitRepresentation.set(1, 3);
		assertFalse(this.bitRepresentation.get(0));
		assertTrue(this.bitRepresentation.get(1));
		assertTrue(this.bitRepresentation.get(2));
		assertEquals(2, this.bitRepresentation.getCardinality());
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void testSetMultiple_IndexOutOfBoundsException_1() {
		this.bitRepresentation.set(1, 4);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void testSetMultiple_IndexOutOfBoundsException_2() {
		this.bitRepresentation.set(4, 6);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSetMultiple_IllegalArgumentException_1() {
		this.bitRepresentation.set(-1, 0);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSetMultiple_IllegalArgumentException_2() {
		this.bitRepresentation.set(2, -1);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSetMultiple_IllegalArgumentException_3() {
		this.bitRepresentation.set(2, 1);
	}
	
	@Test
	public void testClear() {
		this.bitRepresentation.set(0, 3);
		this.bitRepresentation.clear(1);
		
		assertTrue(this.bitRepresentation.get(0));
		assertFalse(this.bitRepresentation.get(1));
		assertTrue(this.bitRepresentation.get(2));
		
		assertEquals(2, this.bitRepresentation.getCardinality());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testClear_IllegalArgumentException() {
		this.bitRepresentation.clear(-1);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void testClear_IndexOutOfBoundsException() {
		this.bitRepresentation.clear(3);
	}
	
 	
	@Test
	public void testClearMultiple() {
		this.bitRepresentation.set(0, 3);
		this.bitRepresentation.clear(1, 3);
		
		assertTrue(this.bitRepresentation.get(0));
		assertFalse(this.bitRepresentation.get(1));
		assertFalse(this.bitRepresentation.get(2));
		
		assertEquals(1, this.bitRepresentation.getCardinality());
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void testSetClear_IndexOutOfBoundsException_1() {
		this.bitRepresentation.clear(1, 4);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void testClearMultiple_IndexOutOfBoundsException_2() {
		this.bitRepresentation.clear(4, 6);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testClearMultiple_IllegalArgumentException_1() {
		this.bitRepresentation.clear(-1, 0);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testClearMultiple_IllegalArgumentException_2() {
		this.bitRepresentation.clear(2, -1);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testClearMultiple_IllegalArgumentException_3() {
		this.bitRepresentation.clear(2, 1);
	}
	
	@Test
	public void testClone() {
		BitRepresentation copy = this.bitRepresentation.clone();
		assertEquals(3, copy.size());
		assertEquals(0, copy.getCardinality());
		for (int idx=0; idx<3; idx++) {
			assertFalse(copy.get(idx));
		}
	}
	
	@Test
	public void testDeepCopy() {
		BitRepresentation copy = this.bitRepresentation.clone();
		copy.set(0, 3);
		
		for (int idx=0; idx<3; idx++) {
			assertTrue(copy.get(idx));
			assertFalse(this.bitRepresentation.get(idx));
		}
	}
	
	@Test
	public void testToString() {
		this.bitRepresentation.set(1);
		final String actual = this.bitRepresentation.toString();
		final String expected = "010";
		assertEquals(expected, actual);
	}
	
	@Test
	public void testParseStringWithSize_1() {
		this.bitRepresentation = BitRepresentation.parse("110", 3);
		assertTrue(this.bitRepresentation.get(0));
		assertTrue(this.bitRepresentation.get(1));
		assertFalse(this.bitRepresentation.get(2));
		assertEquals(3, this.bitRepresentation.size());
		assertEquals(2, this.bitRepresentation.getCardinality());
	}
	
	@Test
	public void testParseStringWithSize_2() {
		this.bitRepresentation = BitRepresentation.parse("110", 5);
		assertFalse(this.bitRepresentation.get(0));
		assertFalse(this.bitRepresentation.get(1));
		assertTrue(this.bitRepresentation.get(2));
		assertTrue(this.bitRepresentation.get(3));
		assertFalse(this.bitRepresentation.get(4));
		assertEquals(5, this.bitRepresentation.size());
		assertEquals(2, this.bitRepresentation.getCardinality());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void IllegalArgumentException() {
		this.bitRepresentation = BitRepresentation.parse("110", 2);
	}
	
	@Test
	public void testParseString() {
		this.bitRepresentation = BitRepresentation.parse("0110");
		assertFalse(this.bitRepresentation.get(0));
		assertTrue(this.bitRepresentation.get(1));
		assertTrue(this.bitRepresentation.get(2));
		assertFalse(this.bitRepresentation.get(3));
		assertEquals(4, this.bitRepresentation.size());
		assertEquals(2, this.bitRepresentation.getCardinality());
	}
	
	@Test
	public void testParseIntWithSize() {
		this.bitRepresentation =  BitRepresentation.parse(3, 4);
		assertFalse(this.bitRepresentation.get(0));
		assertFalse(this.bitRepresentation.get(1));
		assertTrue(this.bitRepresentation.get(2));
		assertTrue(this.bitRepresentation.get(3));
		assertEquals(4, this.bitRepresentation.size());
		assertEquals(2, this.bitRepresentation.getCardinality());
	}
	
	@Test
	public void testParseInt() {
		this.bitRepresentation = BitRepresentation.parse(3);
		assertTrue(this.bitRepresentation.get(0));
		assertTrue(this.bitRepresentation.get(1));
		assertEquals(2, this.bitRepresentation.size());
		assertEquals(2, this.bitRepresentation.getCardinality());
	}
	
	@Test
	public void testPadding() {
		this.bitRepresentation = BitRepresentation.parse("101");
		this.bitRepresentation = BitRepresentation.padding(this.bitRepresentation, 5);
		
		assertFalse(this.bitRepresentation.get(0));
		assertFalse(this.bitRepresentation.get(1));
		assertTrue(this.bitRepresentation.get(2));
		assertFalse(this.bitRepresentation.get(3));
		assertTrue(this.bitRepresentation.get(4));
		
		assertEquals(5, this.bitRepresentation.size());
		assertEquals(2, this.bitRepresentation.getCardinality());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testPadding_IllegalArgumentException() {
		this.bitRepresentation = BitRepresentation.padding(this.bitRepresentation, 1);
	}
	
	@Test
	public void testAnd_1() {
		BitRepresentation bit = BitRepresentation.parse("010");
		BitRepresentation otherBit = BitRepresentation.parse("101");
		bit.and(otherBit);
		
		assertFalse(bit.get(0));
		assertFalse(bit.get(1));
		assertFalse(bit.get(2));
		assertEquals(3, bit.size());
		assertEquals(0, bit.getCardinality());
	}
	
	@Test
	public void testAnd_2() {
		BitRepresentation bit = BitRepresentation.parse("010");
		BitRepresentation otherBit = BitRepresentation.parse("01");
		bit.and(otherBit);
		
		assertFalse(bit.get(0));
		assertFalse(bit.get(1));
		assertFalse(bit.get(2));
		assertEquals(3, bit.size());
		assertEquals(0, bit.getCardinality());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAnd_IllegalArgumentException() {
		BitRepresentation otherBit = BitRepresentation.parse("10011");
		this.bitRepresentation.and(otherBit);
	}
	
	@Test
	public void testOr_1() {
		BitRepresentation bit = BitRepresentation.parse("010");
		BitRepresentation otherBit = BitRepresentation.parse("101");
		bit.or(otherBit);
		
		assertTrue(bit.get(0));
		assertTrue(bit.get(1));
		assertTrue(bit.get(2));
		assertEquals(3, bit.size());
		assertEquals(3, bit.getCardinality());
	}
	
	@Test
	public void testOr_2() {
		BitRepresentation bit = BitRepresentation.parse("010");
		BitRepresentation otherBit = BitRepresentation.parse("01");
		
		bit.or(otherBit);
		
		assertFalse(bit.get(0));
		assertTrue(bit.get(1));
		assertTrue(bit.get(2));
		assertEquals(3, bit.size());
		assertEquals(2, bit.getCardinality());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testOr_IllegalArgumentException() {
		BitRepresentation otherBit = BitRepresentation.parse("10011");
		this.bitRepresentation.or(otherBit);
	}
}
