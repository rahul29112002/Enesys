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

package com.waoss.enesys.cpu;

import com.waoss.enesys.Console;
import com.waoss.enesys.cpu.instructions.Instruction;
import com.waoss.enesys.mem.Addressing;
import com.waoss.enesys.mem.Memory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CentralProcessorTest {

    private Console targetConsole;
    private CentralProcessor target;

    @Before
    public void initTargets() {
        targetConsole = new Console();
        target = new CentralProcessor(targetConsole);
    }

    @Test
    public void registerLoading() throws Exception {
        testBiArgumented(0x0600, 0xa2, 0x0601, 0x44);
    }

    @Test
    public void branching() throws Exception {
        testBiArgumented(0x0600, 0x90, 0x0601, 0x01);
    }

    @Test
    public void carryFlagSetting() throws Exception {
        testUniArgumented(0x0600, 0x38);
        assertTrue(target.getProcessorStatus().isCarryFlagEnabled());
    }

    @Test
    public void carryFlagClearing() throws Exception {
        testUniArgumented(0x0600, 0x18);
        assertFalse(target.getProcessorStatus().isCarryFlagEnabled());
    }

    @Test
    public void interruptClearing() throws Exception {
        testUniArgumented(0x0600, 0x58);
        assertFalse(target.getProcessorStatus().isInterruptFlagEnabled());
    }

    @Test
    public void interruptSetting() throws Exception {
        testUniArgumented(0x0600, 0x78);
        assertFalse(target.getProcessorStatus().isInterruptFlagEnabled());
    }

    @Test
    public void overflowFlagClearing() throws Exception {
        testUniArgumented(0x0600, 0xb8);
        assertFalse(target.getProcessorStatus().isOverflowFlagEnabled());
    }

    @Test
    public void decimalFlagSetting() throws Exception {
        testUniArgumented(0x0600, 0xf8);
        assert target.getProcessorStatus().isDecimalFlagEnabled();
    }

    @Test
    public void aslTest() throws Exception {
        testBiArgumented(0x0600, 0x0e, 0x0601, 0x05);
    }

    @Test
    public void compareTest() throws Exception {
        testBiArgumented(0x0600, 0xc9, 0x0601, 0x00);
        assertTrue(target.getProcessorStatus().isZeroFlagEnabled());
        assertFalse(target.getProcessorStatus().isCarryFlagEnabled());
    }

    @Test
    public void compareXTest() throws Exception {
        testBiArgumented(0x0600, 0xe0, 0x0601, 0x00);
        assertTrue(target.getProcessorStatus().isZeroFlagEnabled());
        assertFalse(target.getProcessorStatus().isCarryFlagEnabled());
    }

    @Test
    public void compareYTest() throws Exception {
        testBiArgumented(0x0600, 0xc0, 0x0601, 0x00);
        assertTrue(target.getProcessorStatus().isZeroFlagEnabled());
        assertFalse(target.getProcessorStatus().isCarryFlagEnabled());
    }

    @Test
    public void indexedIndirectAddressingProcessing() throws Exception {
        final Instruction instruction = new Instruction(0xa1, Addressing.INDEXED_INDIRECT);
        final Memory memory = target.getCompleteMemory();
        target.getXRegister().setValue(1);
        memory.write(1, 5);
        memory.write(2, 6);
        memory.write(0x0605, 5);
        instruction.setCentralProcessor(target);
        instruction.setArguments(0);
        instruction.parseArgumentsAccordingToAddressing();
    }

    @Test
    public void indirectIndexedAddressingProcessing() throws Exception {
        final Instruction instruction = new Instruction(0xa1, Addressing.INDIRECT_INDEXED);
        final Memory memory = target.getCompleteMemory();
        target.getYRegister().setValue(1);
        memory.write(1, 3);
        memory.write(2, 7);
        memory.write(0x0704, 5);
        instruction.setCentralProcessor(target);
        instruction.setArguments(1);
        instruction.parseArgumentsAccordingToAddressing();
    }

    @Test
    public void relativeAddressingProcessing() throws Exception {
        final Instruction instruction = new Instruction(0xd0, Addressing.RELATIVE);
        instruction.setCentralProcessor(target);
        instruction.setArguments(0xf9);
        instruction.parseArgumentsAccordingToAddressing();
    }

    @Test
    public void zeroPageAddressingProcessing() throws Exception {
        final Instruction instruction = new Instruction(0xd0, Addressing.ZERO_PAGE);
        instruction.setCentralProcessor(target);
        target.getCompleteMemory().write(0xff, 5);
        instruction.setArguments(0xff);
        instruction.parseArgumentsAccordingToAddressing();
    }

    @Test
    public void zeroPageXAddressingProcessing() throws Exception {
        final Instruction instruction = testAddressing(Addressing.ZERO_PAGE_X);
        assertEquals(5, (int) instruction.getArguments()[0]);
    }

    @Test
    public void zeroPageYAddressingProcessing() throws Exception {
        final Instruction instruction = testAddressing(Addressing.ZERO_PAGE_Y);
        assertEquals(5, (int) instruction.getArguments()[0]);
    }

    private Instruction testAddressing(Addressing addressing) {
        final Instruction instruction = new Instruction(0xd0, addressing);
        instruction.setCentralProcessor(target);
        target.getCompleteMemory().write(0xff, 5);
        instruction.setArguments(0xff);
        instruction.parseArgumentsAccordingToAddressing();
        return instruction;
    }

    private void testUniArgumented(int address, int instruction) throws Exception {
        testBiArgumented(address, instruction, 0, 0);
    }

    //Testing for two arguments
    private void testBiArgumented(int start, int startValue, int end, int endValue) throws Exception {
        targetConsole.getCompleteMemory().write(start, startValue);
        targetConsole.getCompleteMemory().write(end, endValue);
        startTarget();
    }

    private void startTarget() throws Exception {
        target.start();
        Thread.sleep(300);
        target.interruptThread();
    }
}