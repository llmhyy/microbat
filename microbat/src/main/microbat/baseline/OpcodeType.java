package microbat.baseline;

public enum OpcodeType {
	NOOP, LOAD_CONSTANT, LOAD_VARIABLE, LOAD_FROM_ARRAY, 
	STORE_VARIABLE, STORE_INTO_ARRAY, POP, DUP, SWAP,
	ADD, SUB, MUL, DIV, REM, NEG, LEFT_SHIFT, RIGHT_SHIFT,
	AND, OR, XOR, INCREMENT, CONVERT, IF_EQUALS, IF_NOT_EQUALS,
	COMPARE, IF_COMPARATOR, JUMP, RETURN, GET, PUT, INVOKE,
	NEW, ARRAY_LENGTH, THROW, CHECK, LOCK, UNLOCK, WIDE, NOT_IMPL;
	
	public static OpcodeType parse(short opcode) {
		// TODO: Change to handle short
		if (opcode == 0)
			return NOOP;
		else if (opcode < 16)
			return LOAD_CONSTANT;
		else if (opcode < 46)
			return LOAD_VARIABLE;
		else if (opcode < 54)
			return LOAD_FROM_ARRAY;
		else if (opcode < 79)
			return STORE_VARIABLE;
		else if (opcode < 87)
			return STORE_INTO_ARRAY;
		else if (opcode < 89)
			return POP;
		else if (opcode < 95)
			return DUP;
		else if (opcode < 96)
			return SWAP;
		else if (opcode < 100)
			return ADD;
		else if (opcode < 104)
			return SUB;
		else if (opcode < 108)
			return MUL;
		else if (opcode < 112)
			return DIV;
		else if (opcode < 116)
			return REM;
		else if (opcode < 120)
			return NEG;
		else if (opcode < 122)
			return LEFT_SHIFT;
		else if (opcode < 126)
			return RIGHT_SHIFT;
		else if (opcode < 128)
			return AND;
		else if (opcode < 130)
			return OR;
		else if (opcode < 132)
			return XOR;
		else if (opcode == 132)
			return INCREMENT;
		else if (opcode < 148)
			return CONVERT;
		else if (opcode < 153)
			return COMPARE;
		else if (opcode == 153 || opcode == 159 || opcode == 165 || opcode == 198)
			return IF_EQUALS;
		else if (opcode == 154 || opcode == 160 || opcode == 166 || opcode == 199)
			return IF_NOT_EQUALS;
		else if (opcode < 167)
			return IF_COMPARATOR;
		else if (opcode < 172)
			return JUMP;
		else if (opcode < 178)
			return RETURN;
		else if (opcode == 178 || opcode == 180)
			return GET;
		else if (opcode == 179 || opcode == 181)
			return PUT;
		else if (opcode < 187)
			return INVOKE;
		else if (opcode < 190)
			return NEW;
		else if (opcode == 190)
			return ARRAY_LENGTH;
		else if (opcode == 191)
			return THROW;
		else if (opcode < 194)
			return CHECK;
		else if (opcode == 194)
			return LOCK;
		else if (opcode == 195)
			return UNLOCK;
		else if (opcode == 196)
			return WIDE;
		else if (opcode == 197)
			return NEW;
		else if (opcode == 202)
			return JUMP;
		else
			return NOT_IMPL;
	}
}
