/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008-2012. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.wpi.first.wpilibj;

import java.nio.IntBuffer;

import com.sun.jna.Pointer;

import edu.wpi.first.wpilibj.communication.FRC_NetworkCommunicationsLibrary.tResourceType;
import edu.wpi.first.wpilibj.communication.UsageReporting;
import edu.wpi.first.wpilibj.hal.HALLibrary;
import edu.wpi.first.wpilibj.hal.HALUtil;
import edu.wpi.first.wpilibj.parsing.IInputOutput;
import edu.wpi.first.wpilibj.util.BoundaryException;

/**
 * Class for creating and configuring Analog Triggers
 * 
 * @author dtjones
 */
public class AnalogTrigger implements IInputOutput {

	/**
	 * Exceptions dealing with improper operation of the Analog trigger
	 */
	public class AnalogTriggerException extends RuntimeException {

		/**
		 * Create a new exception with the given message
		 * 
		 * @param message
		 *            the message to pass with the exception
		 */
		public AnalogTriggerException(String message) {
			super(message);
		}

	}

	/**
	 * Where the analog trigger is attached
	 */
	protected Pointer m_port;
	protected int m_index;

	/**
	 * Initialize an analog trigger from a module number and channel. This is
	 * the common code for the two constructors that use a module number and
	 * channel.
	 * 
	 * @param moduleNumber
	 *            The number of the analog module to create this trigger on.
	 * @param channel
	 *            the port to use for the analog trigger
	 */
	protected void initTrigger(final int moduleNumber, final int channel) {
		Pointer port_pointer = HALLibrary.getPortWithModule(
				(byte) moduleNumber, (byte) channel);
		IntBuffer status = IntBuffer.allocate(1);
		IntBuffer index = IntBuffer.allocate(1);
		// XXX: Uncomment when analog has been fixed
		//		m_port = HALLibrary
		//		.initializeAnalogTrigger(port_pointer, index, status);
		//HALUtil.checkStatus(status);
		//m_index = index.get(0);

		UsageReporting.report(tResourceType.kResourceType_AnalogTrigger,
				channel, moduleNumber-1);
	}

	/**
	 * Constructor for an analog trigger given a channel number. The default
	 * module is used in this case.
	 * 
	 * @param channel
	 *            the port to use for the analog trigger
	 */
	public AnalogTrigger(final int channel) {
		initTrigger(AnalogModule.getDefaultAnalogModule(), channel);
	}

	/**
	 * Constructor for an analog trigger given both the module number and
	 * channel.
	 * 
	 * @param moduleNumber
	 *            The number of the analog module to create this trigger on.
	 * @param channel
	 *            the port to use for the analog trigger
	 */
	public AnalogTrigger(final int moduleNumber, final int channel) {
		initTrigger(moduleNumber, channel);
	}

	/**
	 * Construct an analog trigger given an analog channel. This should be used
	 * in the case of sharing an analog channel between the trigger and an
	 * analog input object.
	 * 
	 * @param channel
	 *            the AnalogChannel to use for the analog trigger
	 */
	public AnalogTrigger(AnalogChannel channel) {
		initTrigger(channel.getModuleNumber(), channel.getChannel());
	}

	/**
	 * Release the resources used by this object
	 */
	public void free() {
		IntBuffer status = IntBuffer.allocate(1);
		HALLibrary.cleanAnalogTrigger(m_port, status);
		HALUtil.checkStatus(status);
		m_port = null;
	}

	/**
	 * Set the upper and lower limits of the analog trigger. The limits are
	 * given in ADC codes. If oversampling is used, the units must be scaled
	 * appropriately.
	 * 
	 * @param lower
	 *            the lower raw limit
	 * @param upper
	 *            the upper raw limit
	 */
	public void setLimitsRaw(final int lower, final int upper) {
		if (lower > upper) {
			throw new BoundaryException("Lower bound is greater than upper");
		}
		IntBuffer status = IntBuffer.allocate(1);
		HALLibrary.setAnalogTriggerLimitsRaw(m_port, lower, upper, status);
		HALUtil.checkStatus(status);
	}

	/**
	 * Set the upper and lower limits of the analog trigger. The limits are
	 * given as floating point voltage values.
	 * 
	 * @param lower
	 *            the lower voltage limit
	 * @param upper
	 *            the upper voltage limit
	 */
	public void setLimitsVoltage(double lower, double upper) {
		if (lower > upper) {
			throw new BoundaryException(
					"Lower bound is greater than upper bound");
		}
		// TODO: This depends on the averaged setting. Only raw values will work
		// as is.
		IntBuffer status = IntBuffer.allocate(1);
		HALLibrary.setAnalogTriggerLimitsVoltage(m_port, (float) lower,
				(float) upper, status);
		HALUtil.checkStatus(status);
	}

	/**
	 * Configure the analog trigger to use the averaged vs. raw values. If the
	 * value is true, then the averaged value is selected for the analog
	 * trigger, otherwise the immediate value is used.
	 * 
	 * @param useAveragedValue
	 *            true to use an averaged value, false otherwise
	 */
	public void setAveraged(boolean useAveragedValue) {
		IntBuffer status = IntBuffer.allocate(1);
		HALLibrary.setAnalogTriggerAveraged(m_port,
				(byte) (useAveragedValue ? 1 : 0), status);
		HALUtil.checkStatus(status);
	}

	/**
	 * Configure the analog trigger to use a filtered value. The analog trigger
	 * will operate with a 3 point average rejection filter. This is designed to
	 * help with 360 degree pot applications for the period where the pot
	 * crosses through zero.
	 * 
	 * @param useFilteredValue
	 *            true to use a filterd value, false otherwise
	 */
	public void setFiltered(boolean useFilteredValue) {
		IntBuffer status = IntBuffer.allocate(1);
		HALLibrary.setAnalogTriggerFiltered(m_port,
				(byte) (useFilteredValue ? 1 : 0), status);
		HALUtil.checkStatus(status);
	}

	/**
	 * Return the index of the analog trigger. This is the FPGA index of this
	 * analog trigger instance.
	 * 
	 * @return The index of the analog trigger.
	 */
	public int getIndex() {
		return m_index;
	}

	/**
	 * Return the InWindow output of the analog trigger. True if the analog
	 * input is between the upper and lower limits.
	 * 
	 * @return The InWindow output of the analog trigger.
	 */
	public boolean getInWindow() {
		IntBuffer status = IntBuffer.allocate(1);
		byte value = HALLibrary.getAnalogTriggerInWindow(m_port, status);
		HALUtil.checkStatus(status);
		return value != 0;
	}

	/**
	 * Return the TriggerState output of the analog trigger. True if above upper
	 * limit. False if below lower limit. If in Hysteresis, maintain previous
	 * state.
	 * 
	 * @return The TriggerState output of the analog trigger.
	 */
	public boolean getTriggerState() {
		IntBuffer status = IntBuffer.allocate(1);
		byte value = HALLibrary.getAnalogTriggerTriggerState(m_port, status);
		HALUtil.checkStatus(status);
		return value != 0;
	}

	/**
	 * Creates an AnalogTriggerOutput object. Gets an output object that can be
	 * used for routing. Caller is responsible for deleting the
	 * AnalogTriggerOutput object.
	 * 
	 * @param type
	 *            An enum of the type of output object to create.
	 * @return A pointer to a new AnalogTriggerOutput object.
	 */
	AnalogTriggerOutput createOutput(int type) {
		return new AnalogTriggerOutput(this, type);
	}
}