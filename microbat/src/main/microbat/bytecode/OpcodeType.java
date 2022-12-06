package microbat.bytecode;

public enum OpcodeType {
	NOOP, 
	// Load
	LOAD_CONSTANT, LOAD_VARIABLE, LOAD_FROM_ARRAY, 
	// Store
	STORE_VARIABLE, STORE_INTO_ARRAY, 
	// Pop
	POP,
	// Dup
	DUP,
	// Swap
	SWAP,
	// Operation
	ADD, SUB, MUL, DIV, REM, NEG, 
	// Shift
	LEFT_SHIFT, RIGHT_SHIFT,
	// Boolean operation
	AND, OR, XOR, 
	// Increment
	INCREMENT, 
	// Type convert
	CONVERT, 
	// Compare
	IF_EQUALS, IF_NOT_EQUALS, IF_GREATER, IF_LESS, IF_GREATER_EQUALS, IF_LESS_EQUALS, COMPARE, IF_NULL, IF_NOT_NULL, 
	// JUMP or GOTO
	GOTO, 
	// Deprecated Operation
	DEPRECATED,
	// Switch
	SWTICH,
	// Return
	RETURN, 
	// Get Field Variable
	GET_STATIC_FIELD, GET_FIELD, 
	// Put Field Variable
	PUT_STATIC_FIELD, PUT_FIELD, 
	// Calling Function
	INVOKE,
	// Allocate New Memory
	NEW, 
	// Array Length
	ARRAY_LENGTH,
	// Throw Exception
	THROW, 
	// Inheritance Check
	CHECK, 
	// Thread
	LOCK, UNLOCK, 
	// Wide
	WIDE, 
	// Not Implemented Opcode
	NOT_IMPL;
	
	public static OpcodeType parse(short opcode) {
		if (opcode == 0x00)
			return NOOP;
		else if (opcode >= 0x01 && opcode <= 0x14)
			return LOAD_CONSTANT;
		else if (opcode >= 0x15 && opcode <= 0x2d)
			return LOAD_VARIABLE;
		else if (opcode >= 0x2e && opcode <= 0x35)
			return LOAD_FROM_ARRAY;
		else if (opcode >= 0x36 && opcode <= 0x4e)
			return STORE_VARIABLE;
		else if (opcode >= 0x4f && opcode <= 0x56)
			return STORE_INTO_ARRAY;
		else if (opcode >= 0x57 && opcode <= 0x58)
			return POP;
		else if (opcode >= 0x59 && opcode <= 0x5e)
			return DUP;
		else if (opcode == 0x5f)
			return SWAP;
		else if (opcode >= 0x60 && opcode <= 0x63)
			return ADD;
		else if (opcode >= 0x64 && opcode <= 0x67)
			return SUB;
		else if (opcode >= 0x68 && opcode <= 0x6b)
			return MUL;
		else if (opcode >= 0x6c && opcode <= 0x6f)
			return DIV;
		else if (opcode >= 0x70 && opcode <= 0x73)
			return REM;
		else if (opcode >= 0x74 && opcode <= 0x77)
			return NEG;
		else if (opcode >= 0x78 && opcode <= 0x79)
			return LEFT_SHIFT;
		else if (opcode >= 0x7a && opcode <= 0x7d)
			return RIGHT_SHIFT;
		else if (opcode >= 0x7e && opcode <= 0x7f)
			return AND;
		else if (opcode >= 0x80 && opcode <= 0x81)
			return OR;
		else if (opcode >= 0x82 && opcode <= 0x83)
			return XOR;
		else if (opcode == 0x84)
			return INCREMENT;
		else if (opcode >= 0x85 && opcode <= 0x93)
			return CONVERT;
		else if (opcode >= 0x94 && opcode <= 0x98)
			return COMPARE;
		else if (opcode == 0x99)
			return IF_EQUALS;
		else if (opcode == 0x9a)
			return IF_NOT_EQUALS;
		else if (opcode == 0x9b)
			return IF_LESS;
		else if (opcode == 0x9c)
			return IF_GREATER_EQUALS;
		else if (opcode == 0x9d)
			return IF_GREATER;
		else if (opcode == 0x9e)
			return IF_LESS_EQUALS;
		else if (opcode == 0x9f)
			return IF_EQUALS;
		else if (opcode == 0xa0)
			return IF_NOT_EQUALS;
		else if (opcode == 0xa1)
			return IF_LESS;
		else if (opcode == 0xa2)
			return IF_GREATER_EQUALS;
		else if (opcode == 0xa3)
			return IF_GREATER;
		else if (opcode == 0xa4)
			return IF_LESS_EQUALS;
		else if (opcode == 0xa5)
			return IF_EQUALS;
		else if (opcode == 0xa6)
			return IF_NOT_EQUALS;
		else if (opcode == 0xa7)
			return GOTO;
		else if (opcode >= 0xa8 && opcode <= 0xa9)
			return DEPRECATED;
		else if (opcode >= 0xaa && opcode <= 0xab)
			return SWTICH;
		else if (opcode >= 0xac && opcode <= 0xb1)
			return RETURN;
		else if (opcode == 0xb2)
			return GET_STATIC_FIELD;
		else if (opcode == 0xb3)
			return PUT_STATIC_FIELD;
		else if (opcode == 0xb4)
			return GET_FIELD;
		else if (opcode == 0xb5)
			return GET_STATIC_FIELD;
		else if (opcode >= 0xb6 && opcode <= 0xba)
			return INVOKE;
		else if (opcode >= 0xbb && opcode <= 0xbd)
			return NEW;
		else if (opcode == 0xbe)
			return ARRAY_LENGTH;
		else if (opcode == 0xbf)
			return THROW;
		else if (opcode >= 0xc0 && opcode <= 0xc1)
			return CHECK;
		else if (opcode == 0xc2)
			return LOCK;
		else if (opcode == 0xc3)
			return UNLOCK;
		else if (opcode == 0xc4)
			return WIDE;
		else if (opcode == 0xc5)
			return NEW;
		else if (opcode == 0xc6)
			return IF_NULL;
		else if (opcode == 0xc7)
			return IF_NOT_NULL;
		else if (opcode == 0xc8)
			return GOTO;
		else if (opcode == 0xc9)
			return DEPRECATED;
		else
			return NOT_IMPL;
	}
}
