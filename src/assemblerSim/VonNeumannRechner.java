package assemblerSim;

/**
 * Simulates a VonNeuman-machine
 * @author Tim Borcherding; Marco "Don" Kaulea
 *
 */
public class VonNeumannRechner
{
	/*
	 * Variables
	 */
	Controller controller;
	/*
	 * Program Variables
	 */
	private int accumulator;
	private int valueRegister;
	private int instructionRegister;
	private int programCounter;
	private int addressRegister;
	private int microCounter = 0;
	private int[] ram;
	protected void setRam(int index, int value)
	{
		ram[index] = value;
		controller.updateRAMAnimation(ram);
	}
	boolean jmpFlag = false;
	
	int instruction;
	Opcodes command;
	
	public final static int ACCUMULATOR = 0;
	public final static int VALUEREGISTER = 1;
	public final static int INSTRUCTIONREGISTER = 2;
	public final static int PROGRAMMCOUNTER = 3;
	public final static int ADDRESSREGISTER = 4;
	
	
	int nextStep;	
	public final static int STEP_HALT = 0;
	public final static int STEP_FETCH = 1;
	public final static int STEP_INDIRECT = 2;
	public final static int STEP_EXECUTE = 3;
	
	/**
	 * Create a new instance of VonNeumanRechner
	 * @param nController 
	 * @param nramSize Size of the ram
	 */
	public VonNeumannRechner(Controller nController, int nramSize)
	{
		controller = nController;
		ram = new int[nramSize];
		nextStep = 1;
	}
	
	/**
	 * Resets the microinstruction counter
	 */
	private void resetMicro()
	{
		microCounter = 0;
	}
	
	/**
	 * Sets a new ram content
	 * @param nram The new ram content
	 */
	protected void setRam(int[] nram)
	{
		ram = nram;
		controller.updateRAMAnimation(ram);
	}
	/**
	 * Returns the content of the ram
	 * @return The content of the ram
	 */
	protected int[] getRam()
	{
		return ram;
	}
	/**
	 * Returns the number of lines in the ram
	 * @return The number of lines in the ram
	 */
	protected int getRamSize()
	{
		return ram.length;
	}
	/**
	 * Resets the VonNeumann-Machine
	 */
	protected void reset() 
	{
		
		accumulator=0;
		valueRegister=0;
		instructionRegister=0;
		programCounter=0;
		addressRegister=0;
		controller.setRegister(ACCUMULATOR, accumulator);
		controller.setRegister(VALUEREGISTER, valueRegister);
		controller.setRegister(INSTRUCTIONREGISTER, instructionRegister);
		controller.setRegister(PROGRAMMCOUNTER,programCounter);
		controller.setRegister(ADDRESSREGISTER,addressRegister);
		setRam(new int[ram.length]);
		nextStep = STEP_FETCH;
		resetMicro();
		jmpFlag = false;
		controller.setCycleDisplay("FETCH");
		controller.setLine(0);
	}
	/**
	 * Execute a instruction step on the VonNeumann-Machine 
	 */
	protected void step()
	{
		switch(nextStep)
		{
			case STEP_FETCH:
				controller.setCycleDisplay("FETCH");
				fetch();
				break;
			case STEP_INDIRECT:
				controller.setCycleDisplay("INDIRECT");
				indirect();
				break;
			case STEP_EXECUTE:
				controller.setCycleDisplay("EXECUTE");
				execute();
				break;
		}
	}
	/**
	 * Execute the Fetch-Cycle on the VonNeuman-Machine
	 */
	private void fetch()
	{
		switch(microCounter)
		{
			case 0:
				loadCurrentAddress();
				microCounter++;
				controller.appendEvent("Set Adressregister to Programcounter ("+programCounter+")\n");
				break;
			case 1:
				loadOpcode();
				resetMicro();
				nextStep = STEP_INDIRECT;
				controller.appendEvent("LOAD memorycell "+ addressRegister +" into Instructionregister\n");
		}
	}
	/**
	 * Execute the Indirect-Cycle on the VonNeuman-Machine
	 */
	private void indirect()
	{
		instruction = Integer.rotateRight(instructionRegister, 24)&255;
		try
		{
			command = Opcodes.values()[instruction];
		}
		catch(IndexOutOfBoundsException e)
		{
			command = Opcodes.NOP;
		}
		switch(command)
		{
		case HALT:
		case LOAD:
		case STORE:
		case ADD:
		case SUB:
		case MULT:
		case DIV:
		case MOD:
		case AND:
		case OR:
		case JMP:
		case JMPEQ:
		case JMPNE:
		case JMPGT:
		case JMPLT:
		case JMPGE:
		case JMPLE:
			loadAddress();
			controller.appendEvent("Set Adressregister to "+addressRegister+"\n");
		case LOADM:
		case ADDM:
		case SUBM:
		case MULTM:
		case DIVM:
		case MODM:
		case ANDM:
		case ORM:
		case NOT:
		case NOP:
		case JMPM:
		case JMPEQM:
		case JMPNEM:
		case JMPGTM:
		case JMPLTM:
		case JMPGEM:
		case JMPLEM:
		default:
			nextStep = STEP_EXECUTE;
			break;
		case LOADI:
		case STOREI:
		case ADDI:
		case SUBI:
		case MULTI:
		case DIVI:
		case MODI:
		case ANDI:
		case ORI:
		case JMPI:
		case JMPEQI:
		case JMPNEI:
		case JMPGTI:
		case JMPLTI:
		case JMPGEI:
		case JMPLEI:
			switch(microCounter)
			{
			case 0:
				loadAddress();
				microCounter++;
				controller.appendEvent("Set Adressregister to "+addressRegister+"\n");
				break;
			case 1:
				addressIndirect();
				resetMicro();
				nextStep = STEP_EXECUTE;
				controller.appendEvent("Indirect Adressregister to "+addressRegister+"\n");
				break;
			}
			break;
		}
	}
	/**
	 * Execute the Execute-Cycle on the VonNeuman-Machine
	 */
	private void execute()
	{		
		switch (command)
		{
		case NOP:
		default:
			controller.appendEvent("NOP\n");
			increaseProgramCounter();
			nextStep = STEP_FETCH;
			controller.setLine(0);
			break;
		case HALT:
			controller.appendEvent("HALT\n");
			nextStep = STEP_HALT;
			controller.setCycleDisplay("HALT");
			controller.setLine(0);
			controller.halt();
			break;
		case BREAK:
			controller.appendEvent("Breakpoint reached\n");
			controller.halt();
			increaseProgramCounter();
			nextStep = STEP_FETCH;
			break;
		case LOADI:
		case LOAD:
			loadRamToAcc();
			increaseProgramCounter();
			nextStep = STEP_FETCH;
			break;
		case LOADM:
			loadAccImmediate();
			increaseProgramCounter();
			nextStep = STEP_FETCH;
			break;
		case STOREI:
		case STORE:
			storeAccToRam();
			increaseProgramCounter();
			nextStep = STEP_FETCH;
			break;
		case ADDI:
		case ADD:
			switch(microCounter)
			{
			case 0:
				loadRamToValue();
				microCounter++;
			break;
			case 1:
				add();
				increaseProgramCounter();
				resetMicro();
				nextStep = STEP_FETCH;
				break;
			}
			break;
		case ADDM:
			switch(microCounter)
			{
			case 0:
				loadValueImmediate();
				microCounter++;
			break;
			case 1:
				add();
				increaseProgramCounter();
				resetMicro();
				nextStep = STEP_FETCH;
				break;
			}
			break;
		case SUBI:
		case SUB:
			switch(microCounter)
			{
			case 0:
				loadRamToValue();
				microCounter++;
			break;
			case 1:
				sub();
				increaseProgramCounter();
				resetMicro();
				nextStep = STEP_FETCH;
				break;
			}
			break;
		case SUBM:
			switch(microCounter)
			{
			case 0:
				loadValueImmediate();
				microCounter++;
			break;
			case 1:
				sub();
				increaseProgramCounter();
				resetMicro();
				nextStep = STEP_FETCH;
				break;
			}
			break;
		case MULTI:
		case MULT:
			switch(microCounter)
			{
			case 0:
				loadRamToValue();
				microCounter++;
			break;
			case 1:
				mult();
				increaseProgramCounter();
				resetMicro();
				nextStep = STEP_FETCH;
				break;
			}
			break;
		case MULTM:
			switch(microCounter)
			{
			case 0:
				loadValueImmediate();
				microCounter++;
			break;
			case 1:
				mult();
				increaseProgramCounter();
				resetMicro();
				nextStep = STEP_FETCH;
				break;
			}
			break;
		case DIVI:
		case DIV:
			switch(microCounter)
			{
			case 0:
				loadRamToValue();
				microCounter++;
			break;
			case 1:
				div();
				increaseProgramCounter();
				resetMicro();
				nextStep = STEP_FETCH;
				break;
			}
			break;
		case DIVM:
			switch(microCounter)
			{
			case 0:
				loadValueImmediate();
				microCounter++;
			break;
			case 1:
				div();
				increaseProgramCounter();
				resetMicro();
				nextStep = STEP_FETCH;
				break;
			}
			break;
		case MODI:
		case MOD:
			switch(microCounter)
			{
			case 0:
				loadRamToValue();
				microCounter++;
			break;
			case 1:
				mod();
				increaseProgramCounter();
				resetMicro();
				nextStep = STEP_FETCH;
				break;
			}
			break;
		case MODM:
			switch(microCounter)
			{
			case 0:
				loadValueImmediate();
				microCounter++;
			break;
			case 1:
				mod();
				increaseProgramCounter();
				resetMicro();
				nextStep = STEP_FETCH;
				break;
			}
			break;
		case ANDI:
		case AND:
			switch(microCounter)
			{
			case 0:
				loadRamToValue();
				microCounter++;
			break;
			case 1:
				and();
				increaseProgramCounter();
				resetMicro();
				nextStep = STEP_FETCH;
				break;
			}
			break;
		case ANDM:
			switch(microCounter)
			{
			case 0:
				loadValueImmediate();
				microCounter++;
			break;
			case 1:
				and();
				increaseProgramCounter();
				resetMicro();
				nextStep = STEP_FETCH;
				break;
			}
			break;
		case ORI:
		case OR:
			switch(microCounter)
			{
			case 0:
				loadRamToValue();
				microCounter++;
			break;
			case 1:
				or();
				increaseProgramCounter();
				resetMicro();
				nextStep = STEP_FETCH;
				break;
			}
			break;
		case ORM:
			switch(microCounter)
			{
			case 0:
				loadValueImmediate();
				microCounter++;
			break;
			case 1:
				or();
				increaseProgramCounter();
				resetMicro();
				nextStep = STEP_FETCH;
				break;
			}
			break;
		case NOT:
			not();
			increaseProgramCounter();
			nextStep = STEP_FETCH;
			break;
		case JMP:
		case JMPI:
			loadJmp();
			break;
		case JMPM:
			loadJmpImmediate();
			break;
		case JMPEQ:
		case JMPEQI:
			checkEQ();
			checkCond();
			break;
		case JMPNE:
		case JMPNEI:
			checkNE();
			checkCond();
			break;
		case JMPGT:
		case JMPGTI:
			checkGT();
			checkCond();
			break;
		case JMPLT:
		case JMPLTI:
			checkLT();
			checkCond();
			break;
		case JMPGE:
		case JMPGEI:
			checkGE();
			checkCond();
			break;
		case JMPLE:
		case JMPLEI:
			checkLE();
			checkCond();
			break;
		case JMPEQM:
			checkEQ();
			checkCondM();
			break;
		case JMPNEM:
			checkNE();
			checkCondM();
			break;
		case JMPGTM:
			checkGT();
			checkCondM();
			break;
		case JMPLTM:
			checkLT();
			checkCondM();
			break;
		case JMPGEM:
			checkGE();
			checkCondM();
			break;
		case JMPLEM:
			checkLE();
			checkCondM();
			break;
		}
	}
	/**
	 * Increase the programmcounter of the VonNeuman-Machine by 1 and HALT, if the programmcounter points to the last line of the ram of the VonNeuman-Machine
	 */
	private void increaseProgramCounter()
	{
		if(programCounter < (ram.length-1))
		{		
			programCounter++;
			controller.setRegister(PROGRAMMCOUNTER, programCounter);
			controller.appendEvent("Increase Programcounter by 1 ("+programCounter+")\n");
		}
		else
		{
			nextStep = STEP_HALT;
			controller.setCycleDisplay("HALT");
			controller.halt();
		}
	}
	/**
	 * use the adress set in the adress register to load the new adress from ram
	 */
	private void addressIndirect()
	{
		addressRegister = ram[addressRegister]%ram.length;
		controller.setRegister(ADDRESSREGISTER, addressRegister);
		controller.setLine(1);
	}
	/**
	 * Load a value into the valueregister directly from the instruction register
	 */
	private void loadValueImmediate()
	{
		valueRegister = instructionRegister&0xFFFFFF/**16777215**/;
		controller.setRegister(VALUEREGISTER, valueRegister);
		controller.setLine(9);
		controller.appendEvent("Load value "+ valueRegister +" into Valueregister\n");
	}
	/**
	 * Load a value into the accumulator directly from the instruction register
	 */
	private void loadAccImmediate()
	{
		accumulator = instructionRegister&0xFFFFFF/**16777215**/;
		controller.setRegister(ACCUMULATOR, accumulator);
		controller.setLine(7);
		controller.appendEvent("LOAD value "+ accumulator +" into Accumulator\n");
	}
	/**
	 * Load the adress from the instructionregister into the adressregister
	 */
	private void loadAddress()
	{
		addressRegister = instructionRegister&0xFFFFFF/**16777215**/%ram.length;	//line 11
		controller.setRegister(ADDRESSREGISTER, addressRegister);
		controller.setLine(10);
	}
	/**
	 * Load the content of the ramcell, which is pointed to by the adressregister into the valueregister.
	 */
	private void loadRamToValue()
	{
		valueRegister = ram[addressRegister];
		controller.setRegister(VALUEREGISTER, valueRegister);
		controller.setLine(4);
		controller.appendEvent("Load memorycell "+ addressRegister +" ("+valueRegister+") into Valueregister\n");
	}
	/**
	 * Load the content of the ramcell, which is pointed to by the adressregister into the accumulator. 
	 */
	private void loadRamToAcc()
	{
		accumulator = ram[addressRegister];
		controller.setRegister(ACCUMULATOR, accumulator);
		controller.setLine(2);
		controller.appendEvent("LOAD memorycell "+ addressRegister +" ("+accumulator+") into Accumulator\n");
	}
	/**
	 * Stores the Accumulator to the ramcell which is pointet to by the adressregister.
	 */
	private void storeAccToRam()
	{
		setRam(addressRegister,accumulator);
		controller.setLine(13);
		controller.appendEvent("STORE Accumulator ("+accumulator+") to memorycell "+ addressRegister +"\n");
	}
	/**
	 * Adds the accumulator and the valueregister and stores the result in the accumulator
	 */
	private void add()
	{
		accumulator = accumulator + valueRegister;
		controller.setRegister(ACCUMULATOR, accumulator);
		controller.setLine(11);
		controller.appendEvent("ADD Valueregister ("+valueRegister+") to Accumulator. Result: "+accumulator+"\n");
	}
	/**
	 * Substract the accumulator by the valueregister and stores the result in the accumulator
	 */
	private void sub()
	{
		accumulator = accumulator - valueRegister;
		controller.setRegister(ACCUMULATOR, accumulator);
		controller.setLine(11);
		controller.appendEvent("Substract Valueregister ("+valueRegister+") from Accumulator. Result: "+accumulator+"\n");
	}
	/**
	 * Multiply the accumulator by the valueregister and stores the result in the accumulator
	 */
	private void mult()
	{
		accumulator = accumulator * valueRegister;
		controller.setRegister(ACCUMULATOR, accumulator);
		controller.setLine(11);
		controller.appendEvent("Multiply Valueregister ("+valueRegister+") and Accumulator. Result: "+accumulator+"\n");
	}
	/**
	 * Divide the accumulator by the valueregister and stores the result in the accumulator
	 */
	private void div()
	{
		accumulator = accumulator / valueRegister;
		controller.setRegister(ACCUMULATOR, accumulator);
		controller.setLine(11);
		controller.appendEvent("Devide Accumulator by Valueregister ("+valueRegister+"). Result: "+accumulator+"\n");
	}
	/**
	 *  Calculate the remainder of the accumulator by the valueregister and stores the result in the accumulator 
	 */
	private void mod()
	{
		accumulator = accumulator % valueRegister;
		controller.setRegister(ACCUMULATOR, accumulator);
		controller.setLine(11);
		controller.appendEvent("Accumulator Modulo Valueregister ("+valueRegister+"). Result: "+accumulator+"\n");
	}
	/**
	 * Calculate the logical conjunction "and" of the accumulator and valueregister and stores the result in the accumulator
	 */
	private void and()
	{
		accumulator = accumulator & valueRegister;
		controller.setRegister(ACCUMULATOR, accumulator);
		controller.setLine(11);
		controller.appendEvent("Accumulator AND Valueregister ("+valueRegister+"). Result: "+accumulator+"\n");
	}
	/**
	 * Calculate the logical conjunction "or" of the accumulator and valueregister and stores the result in the accumulator
	 */
	private void or()
	{
		accumulator = accumulator | valueRegister;
		controller.setRegister(ACCUMULATOR, accumulator);
		controller.setLine(11);
		controller.appendEvent("Accumulator OR Valueregister ("+valueRegister+"). Result: "+accumulator+"\n");
	}
	/**
	 * Invert the Accumulator
	 */
	private void not()
	{
		accumulator = ~accumulator;
		controller.setRegister(ACCUMULATOR, accumulator);
		controller.setLine(12);
		controller.appendEvent("Invert Accumulator. Result: "+accumulator+"\n");
	}
	/**
	 * Load the adress for the jump into the programcounter directly from the instruction register
	 */
	private void loadJmpImmediate()
	{
		programCounter = instructionRegister&0xFFFFFF/**16777215**/%ram.length;
		nextStep = STEP_FETCH;
		controller.setRegister(PROGRAMMCOUNTER, programCounter);
		controller.setLine(8);
		controller.appendEvent("Jump: set Programmcounter to "+programCounter+"\n");
	}
	/**
	 * Load the adress for the jump into the programcounter from the ramcell pointed to by the adressregister
	 */
	private void loadJmp()
	{
		programCounter = ram[addressRegister];
		nextStep = STEP_FETCH;
		controller.setRegister(PROGRAMMCOUNTER, programCounter);
		controller.setLine(3);
		controller.appendEvent("Jump: set Programmcounter to "+programCounter+"\n");
	}
	/**
	 * Sets the jump flag if accumulator is equal to zero
	 */
	private void checkEQ()
	{
			jmpFlag = (accumulator == 0);			
	}
	/**
	 * Sets the jump flag if accumulator is not equal to zero
	 */
	private void checkNE()
	{
			jmpFlag = (accumulator != 0);			
	}
	/**
	 * Sets the jump flag if accumulator is greater than zero
	 */
	private void checkGT()
	{
			jmpFlag = (accumulator > 0);			
	}
	/**
	 * Sets the jump flag if accumulator is less than zero
	 */
	private void checkLT()
	{
			jmpFlag = (accumulator < 0);			
	}
	/**
	 * Sets the jump flag if accumulator is greater than or equal to zero
	 */
	private void checkGE()
	{
			jmpFlag = (accumulator >= 0);	
	}
	/**
	 * Sets the jump flag if accumulator is less than or equal to zero
	 */
	private void checkLE()
	{
			jmpFlag = (accumulator <= 0);
	}
	/**
	 * Checks the condition and jumps to the adress pointed to by the adressregister
	 */
	private void checkCond()
	{
		if(jmpFlag)
		{
			loadJmp();
			jmpFlag = false;
		}
		else
		{
			nextStep = STEP_FETCH;
			increaseProgramCounter();
			controller.appendEvent("Jump: Jump condition is not fulfilled\n");
		}
	}
	/**
	 * Checks the condition and jumps to the adress set in the instructionregister
	 */
	private void checkCondM()
	{
		if(jmpFlag)
		{
			loadJmpImmediate();
			jmpFlag = false;
		}
		else
		{
			nextStep = STEP_FETCH;
			increaseProgramCounter();
			controller.appendEvent("Jump: Jump condition is not fulfilled\n");
		}
	}
	/**
	 * Loads the adress from the programcounter into the adressregister
	 */
	private void loadCurrentAddress()
	{
		addressRegister = programCounter%ram.length; 
		controller.setRegister(ADDRESSREGISTER, addressRegister);
		controller.setLine(6);
	}
	/**
	 * Load the content of the ramcell, which is pointed to by the adressregister into the instructionregister 
	 */
	private void loadOpcode()
	{
		instructionRegister = ram[addressRegister];
		controller.setRegister(INSTRUCTIONREGISTER,instructionRegister);
		controller.setLine(5);
	}
}
