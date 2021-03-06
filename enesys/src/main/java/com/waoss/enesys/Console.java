/*
 * Enesys : An NES Emulator
 * Copyright (C) 2017  Rahul Chhabra and Waoss
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.waoss.enesys;

import com.google.gson.GsonBuilder;
import com.waoss.enesys.cpu.CentralProcessor;
import com.waoss.enesys.cpu.registers.*;
import com.waoss.enesys.mem.CompleteMemory;
import com.waoss.enesys.mem.Memory;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>A bean that Represents the complete NES system.
 * Contains everything from the memory to the registers to everything else.
 * Primarily contains atomically referenced fields and their getters and setters</p>
 */
public class Console implements Serializable {

    /**
     * The serial version UID.Useful during serialization
     */
    public static final long serialVersionUID = 12325890890L;

    private final AtomicReference<CompleteMemory> completeMemory = new AtomicReference<>(new CompleteMemory());
    private final AtomicReference<ProcessorStatus> processorStatus = new AtomicReference<>(new ProcessorStatus());
    private final AtomicReference<AccumalativeRegister> ARegister = new AtomicReference<>(new AccumalativeRegister());
    private final AtomicReference<XRegister> XRegister = new AtomicReference<>(new XRegister());
    private final AtomicReference<YRegister> YRegister = new AtomicReference<>(new YRegister());
    private final AtomicReference<StackPointer> stackPointer = new AtomicReference<>(new StackPointer());
    private final AtomicReference<ProgramCounter> programCounter = new AtomicReference<>(
            new ProgramCounter((short) 0x0600));
    private final AtomicReference<CentralProcessor> centralProcessor = new AtomicReference<>(
            new CentralProcessor(this));

    public ProcessorStatus getProcessorStatus() {
        return processorStatus.get();
    }

    public void setProcessorStatus(ProcessorStatus processorStatus) {
        this.processorStatus.set(processorStatus);
    }

    public AccumalativeRegister getARegister() {
        return ARegister.get();
    }

    public void setARegister(AccumalativeRegister ARegister) {
        this.ARegister.set(ARegister);
    }

    public XRegister getXRegister() {
        return XRegister.get();
    }

    public void setXRegister(XRegister XRegister) {
        this.XRegister.set(XRegister);
    }

    public YRegister getYRegister() {
        return YRegister.get();
    }

    public void setYRegister(YRegister YRegister) {
        this.YRegister.set(YRegister);
    }

    public StackPointer getStackPointer() {
        return stackPointer.get();
    }

    public void setStackPointer(StackPointer stackPointer) {
        this.stackPointer.set(stackPointer);
    }

    public ProgramCounter getProgramCounter() {
        return programCounter.get();
    }

    public void setProgramCounter(ProgramCounter programCounter) {
        this.programCounter.set(programCounter);
    }

    public CompleteMemory getCompleteMemory() {
        return completeMemory.get();
    }

    public void setCompleteMemory(CompleteMemory completeMemory) {
        this.completeMemory.set(completeMemory);
    }

    public CentralProcessor getCentralProcessor() {
        return centralProcessor.get();
    }

    public void setCentralProcessor(CentralProcessor centralProcessor) {
        this.centralProcessor.set(centralProcessor);
    }

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Console.class,
                new ConsoleAdapter()).create().toJson(this);
    }

    /**
     * Loads a set of binaries at the given program counter
     *
     * @param binaries
     *         The binaries to load
     * @param pc
     *         The program counter to load them at
     */
    public void loadBinaries(int[] binaries, int pc) {
        Memory memory = getCompleteMemory();
        for (int i = 0; i < binaries.length; i++) { //iterating
            int binary = binaries[i];
            memory.write(pc + i, binary);
        }
        getProgramCounter().setValue(pc);
    }

    /**
     * Loads a set of binaries and executes them
     *
     * @param binaries
     *         The binaries to load
     * @param pc
     *         The program and counter to load them at
     *
     * @see #loadBinaries(int[], int)
     */
    public void loadAndExecuteBinaries(int[] binaries, int pc) {
        loadBinaries(binaries, pc);
        centralProcessor.get().start();
    }
}
