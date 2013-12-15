/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008-2012. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.wpi.first.wpilibj;

import java.nio.IntBuffer;

import com.sun.jna.Pointer;

import edu.wpi.first.wpilibj.communication.FRC_NetworkCommunicationsLibrary.tModuleType;
import edu.wpi.first.wpilibj.hal.HALLibrary;
import edu.wpi.first.wpilibj.hal.HALUtil;

/**
 * Analog Module class.
 * Each module can independently sample its channels at a configurable rate.
 * There is a 64-bit hardware accumulator associated with channel 1 on each module.
 * The accumulator is attached to the output of the oversample and average engine so that the center
 * value can be specified in higher resolution resulting in less error.
 */
public class AnalogModule extends Module {

    /**
     * The time base used by the module
     */
    public static final int kTimebase = 40000000;
    /**
     * The default number of Oversample bits to use
     */
    public static final int kDefaultOversampleBits = 0;
    /**
     * The default number of averaging bits to use
     */
    public static final int kDefaultAverageBits = 7;
    /**
     * The default sample rate
     */
    public static final double kDefaultSampleRate = 50000.0;
    private Pointer[] m_ports;

    /**
     * Get an instance of an Analog Module.
     *
     * Singleton analog module creation where a module is allocated on the first use
     * and the same module is returned on subsequent uses.
     *
     * @param moduleNumber The index of the analog module to get (1 or 2).
     * @return The AnalogModule.
     */
    public static synchronized AnalogModule getInstance(final int moduleNumber) {
        checkAnalogModule(moduleNumber);
        return (AnalogModule) getModule(tModuleType.kModuleType_Analog, moduleNumber);
    }

    /**
     * Create a new instance of an analog module.
     *
     * Create an instance of the analog module object. Initialize all the parameters
     * to reasonable values on start.
     * Setting a global value on an analog module can be done only once unless subsequent
     * values are set the previously set value.
     * Analog modules are a singleton, so the constructor is never called outside of this class.
     *
     * @param moduleNumber The index of the analog module to create (1 or 2).
     */
    protected AnalogModule(final int moduleNumber) {
        super(tModuleType.kModuleType_Analog, moduleNumber);

        m_ports = new Pointer[8];
        for (int i = 0; i < SensorBase.kAnalogChannels; i++) {
            Pointer port_pointer = HALLibrary.getPortWithModule((byte) moduleNumber, (byte) (i+1));
            IntBuffer status = IntBuffer.allocate(1);
	    // XXX: Uncomment when analogchannel has been fixed
//            m_ports[i] = HALLibrary.initializeAnalogPort(port_pointer, status);
            HALUtil.checkStatus(status);
        }
    }

    /**
     * Set the sample rate on the module.
     *
     * This is a global setting for the module and effects all channels.
     *
     * @param samplesPerSecond
     *            The number of samples per channel per second.
     */
    public void setSampleRate(final double samplesPerSecond) {
        // TODO: This will change when variable size scan lists are implemented.
        // TODO: Need float comparison with epsilon.
        IntBuffer status = IntBuffer.allocate(1);
        HALLibrary.setAnalogSampleRateWithModule((byte) m_moduleNumber, (float) samplesPerSecond, status);
        HALUtil.checkStatus(status);
    }

    /**
     * Get the current sample rate on the module.
     *
     * This assumes one entry in the scan list. This is a global setting for the
     * module and effects all channels.
     *
     * @return Sample rate.
     */
    public double getSampleRate() {
        IntBuffer status = IntBuffer.allocate(1);
        double value = HALLibrary.getAnalogSampleRateWithModule((byte) m_moduleNumber, status);
        HALUtil.checkStatus(status);
        return value;
    }

    /**
     * Set the number of averaging bits.
     *
     * This sets the number of averaging bits. The actual number of averaged
     * samples is 2**bits. The averaging is done automatically in the FPGA.
     *
     * @param channel
     *            Analog channel to configure.
     * @param bits
     *            Number of bits to average.
     */
    public void setAverageBits(final int channel, final int bits) {
        IntBuffer status = IntBuffer.allocate(1);
        HALLibrary.setAnalogAverageBits(m_ports[channel], bits, status);
        HALUtil.checkStatus(status);
    }

    /**
     * Get the number of averaging bits.
     *
     * This gets the number of averaging bits from the FPGA. The actual number
     * of averaged samples is 2**bits. The averaging is done automatically in
     * the FPGA.
     *
     * @param channel Channel to address.
     * @return Bits to average.
     */
    public int getAverageBits(final int channel) {
        IntBuffer status = IntBuffer.allocate(1);
        int value = HALLibrary.getAnalogAverageBits(m_ports[channel], status);
        HALUtil.checkStatus(status);
        return value;
    }

    /**
     * Set the number of oversample bits.
     *
     * This sets the number of oversample bits. The actual number of oversampled values is 2**bits.
     * Use oversampling to improve the resolution of your measurements at the expense of sampling rate.
     * The oversampling is done automatically in the FPGA.
     *
     * @param channel Analog channel to configure.
     * @param bits Number of bits to oversample.
     */
    public void setOversampleBits(final int channel, final int bits) {
        IntBuffer status = IntBuffer.allocate(1);
        HALLibrary.setAnalogOversampleBits(m_ports[channel], bits, status);
        HALUtil.checkStatus(status);
    }

    /**
     * Get the number of oversample bits.
     *
     * This gets the number of oversample bits from the FPGA. The actual number
     * of oversampled values is 2**bits. The oversampling is done automatically
     * in the FPGA.
     *
     * @param channel Channel to address.
     * @return Bits to oversample.
     */
    public int getOversampleBits(final int channel) {
        IntBuffer status = IntBuffer.allocate(1);
        int value = HALLibrary.getAnalogOversampleBits(m_ports[channel], status);
        HALUtil.checkStatus(status);
        return value;
    }

    /**
     * Get the raw analog value.
     *
     * Get the analog value as it is returned from the D/A converter.
     *
     * @param channel Channel number to read.
     * @return Raw value.
     */
    public int getValue(final int channel) {
        IntBuffer status = IntBuffer.allocate(1);
        int value = HALLibrary.getAnalogValue(m_ports[channel], status);
        HALUtil.checkStatus(status);
        return value;
    }

    /**
     * Get the raw averaged and oversampled value.
     *
     * @param channel Channel number to read.
     * @return The averaged and oversampled raw value.
     */
    public int getAverageValue(final int channel) {
        IntBuffer status = IntBuffer.allocate(1);
        int value = HALLibrary.getAnalogAverageValue(m_ports[channel], status);
        HALUtil.checkStatus(status);
        return value;
    }

    /**
     * Convert a voltage to a raw value for a specified channel.
     *
     * This process depends on the calibration of each channel, so the channel
     * must be specified.
     *
     * @todo This assumes raw values. Oversampling not supported as is.
     *
     * @param channel
     *            The channel to convert for.
     * @param voltage
     *            The voltage to convert.
     * @return The raw value for the channel.
     */
    public int voltsToValue(final int channel, final double voltage) {
        // wpi_assert(voltage >= -10.0 && voltage <= 10.0);
        IntBuffer status = IntBuffer.allocate(1);
        int value = HALLibrary.getAnalogVoltsToValue(m_ports[channel], (float) voltage, status);
        HALUtil.checkStatus(status);
        return value;
    }

    /**
     * Get the voltage.
     *
     * Return the voltage from the A/D converter.
     *
     * @param channel The channel to read.
     * @return The scaled voltage from the channel.
     */
    public double getVoltage(final int channel) {
        IntBuffer status = IntBuffer.allocate(1);
        double value = HALLibrary.getAnalogVoltage(m_ports[channel], status);
        HALUtil.checkStatus(status);
        return value;
    }

    /**
     * Get the averaged voltage.
     *
     * Return the averaged voltage from the A/D converter.
     *
     * @param channel The channel to read.
     * @return The scaled averaged and oversampled voltage from the channel.
     */
    public double getAverageVoltage(final int channel) {
        IntBuffer status = IntBuffer.allocate(1);
        double value = HALLibrary.getAnalogAverageVoltage(m_ports[channel], status);
        HALUtil.checkStatus(status);
        return value;
    }

    /**
     * Get the LSB Weight portion of the calibration for a channel.
     *
     * @param channel The channel to get calibration data for.
     * @return LSB Weight calibration point.
     */
    public long getLSBWeight(final int channel) {
        // TODO: add scaling to make this worth while.
        IntBuffer status = IntBuffer.allocate(1);
        long value = HALLibrary.getAnalogLSBWeight(m_ports[channel], status);
        HALUtil.checkStatus(status);
        return value;
    }

    /**
     * Get the Offset portion of the calibration for a channel.
     *
     * @param channel The channel to get calibration data for.
     * @return Offset calibration point.
     */
    public int getOffset(final int channel) {
        // TODO: add scaling to make this worth while.
        // TODO: actually use this function.
        IntBuffer status = IntBuffer.allocate(1);
        int value = HALLibrary.getAnalogOffset(m_ports[channel], status);
        HALUtil.checkStatus(status);
        return value;
    }
}