/******************************************************************************************************************
 * File:ECSMonitor.java
 * Course: 17655
 * Project: Assignment A2
 * Copyright: Copyright (c) 2009 Carnegie Mellon University
 * Versions:
 *  1.0 March 2009 - Initial rewrite of original assignment 2 (ajl).
 *
 * Description:
 *
 * This class monitors the environmental control systems that control museum temperature and humidity. In addition to
 * monitoring the temperature and humidity, the ECSMonitor also allows a user to set the humidity and temperature
 * ranges to be maintained. If temperatures exceed those limits over/under alarm indicators are triggered.
 *
 * Parameters: IP address of the message manager (on command line). If blank, it is assumed that the message manager is
 * on the local machine.
 *
 * Internal Methods:
 *  static private void Heater(MessageManagerInterface ei, boolean ON )
 *  static private void Chiller(MessageManagerInterface ei, boolean ON )
 *  static private void Humidifier(MessageManagerInterface ei, boolean ON )
 *  static private void Dehumidifier(MessageManagerInterface ei, boolean ON )
 *
 ******************************************************************************************************************/
import InstrumentationPackage.*;
import MessagePackage.*;
import Robustness.Robust;

import java.io.*;
import java.rmi.RemoteException;

class ECSMonitor extends Thread
{

	private MessageManagerInterface em = null; // Interface object to the message manager
	private String MsgMgrIP = null;             // Message Manager IP address
	private float TempRangeHigh = 75;       // These parameters signify the temperature and humidity ranges in terms
	private float TempRangeLow = 70;             // of high value and low values. The ECSmonitor will attempt to maintain
	private float HumiRangeHigh = 55;       // this temperature and humidity. Temperatures are in degrees Fahrenheit
	private float HumiRangeLow = 45;             // and humidity is in relative humidity percentage.
	boolean Registered = true;             // Signifies that this class is registered with an message manager.
	MessageWindow mw = null;               // This is the message window
	Indicator ti;                       // Temperature indicator
	Indicator hi;                       // Humidity indicator

	public ECSMonitor()
	{
		// message manager is on the local system

		try
		{
			// Here we create an message manager interface object. This assumes
			// that the message manager is on the local machine
			// When doing MessageQueue registration, pass the message type it will send.
			em = new MessageManagerInterface(MessageType.Monitor);

		}

		catch (Exception e)
		{
			System.out.println("ECSMonitor::Error instantiating message manager interface: " + e);
			Registered = false;

		} // catch

	} //Constructor

	public ECSMonitor( String MsgIpAddress )
	{
		// message manager is not on the local system

		MsgMgrIP = MsgIpAddress;

		try
		{
			// Here we create an message manager interface object. This assumes
			// that the message manager is NOT on the local machine
			em = new MessageManagerInterface(MessageType.Monitor, MsgMgrIP);
		}

		catch (Exception e)
		{
			System.out.println("ECSMonitor::Error instantiating message manager interface: " + e);
			Registered = false;

		} // catch

	} // Constructor

	public void run()
	{
		Message Msg = null;             // Message object
		MessageQueue eq = null;          // Message Queue
		int MsgId = 0;             // User specified message ID
		float CurrentTemperature = 0;  // Current temperature as reported by the temperature sensor
		float CurrentHumidity= 0;     // Current relative humidity as reported by the humidity sensor
		int    Delay = 1000;           // The loop delay (1 second)
		boolean Done = false;        // Loop termination flag
		boolean ON = true;          // Used to turn on heaters, chillers, humidifiers, and dehumidifiers
		boolean OFF = false;         // Used to turn off heaters, chillers, humidifiers, and dehumidifiers

		long maxOffLineTime = 10000;

		long tempMsgQId = -1;  //Used to record the MessageQueueID of the primary temperature sensor.
		long humiMsgQId = -1;  //Used to record the current MessageQueueID of the primary humidity sensor.
		long tempCtrlMsgQId = -1; //Used to record the current MessageQueueID of the primary temperature controller.
		long humiCtrlMsgQId = -1; //Used to record the current MessageQueueID of the primary humidity controller.

		long tempReadingTime = 0; // last time of temperature update
		long humiReadingTime = 0;  //last time of humidity update
		long tempCtrlAliveTime = 0; // last time of receiving confirm message from temperature controller
		long humiCtrlAliveTime = 0;// last time of receiving confirm message from humidity controller


		if (em != null)
		{
			// Now we create the ECS status and message panel
			// Note that we set up two indicators that are initially yellow. This is
			// because we do not know if the temperature/humidity is high/low.
			// This panel is placed in the upper left hand corner and the status
			// indicators are placed directly to the right, one on top of the other

			mw = new MessageWindow("ECS Monitoring Console", 0, 0);
			ti = new Indicator ("TEMP UNK", mw.GetX()+ mw.Width(), 0);
			hi = new Indicator ("HUMI UNK", mw.GetX()+ mw.Width(), (int)(mw.Height()/2), 2 );

			mw.WriteMessage( "Registered with the message manager." );

			try
			{
				mw.WriteMessage("   Participant id: " + em.GetMyId() );
				mw.WriteMessage("   Registration Time: " + em.GetRegistrationTime() );

			} // try

			catch (Exception e)
			{
				System.out.println("Error:: " + e);

			} // catch

			/********************************************************************
			 ** Here we start the main simulation loop
			 *********************************************************************/
			//initialize the last reading time.
			tempReadingTime = System.currentTimeMillis();
			humiReadingTime = System.currentTimeMillis();
			tempCtrlAliveTime = System.currentTimeMillis();
			humiCtrlAliveTime = System.currentTimeMillis();

			while (!Done)
			{
				// Here we get our message queue from the message manager

				try
				{
					eq = em.GetMessageQueue();

				} // try
				catch( Exception e )
				{
					// if failed to get message Queue, it means probably the MessageManager failed.
					mw.WriteMessage("Error getting message queue::" + e );
					mw.WriteMessage("Detected MessageManager lost connected, trying to restart MessageManager ... ");
					mw.WriteMessage("...");
					//restart the Message Manager here:
					Process p = Robust.startNewJava("MessageManager");
					if(p.isAlive()){
						System.out.println("New MessageManager has been restart up!");
						try {
							Thread.sleep(Robust.WAITING_TIME_FOR_RESTART_MSG_MGR);
							// Reconnect to the new Message Manager
							em = Robust.newMsgMgr(MessageType.Monitor);
						}catch (InterruptedException e1){
							e1.printStackTrace();
						} catch (RemoteException e1) {
							//do nothing and retry
						}
					}
					continue;
				} // catch

				// If there are messages in the queue, we read through them.
				// We are looking for MessageIDs = 1 or 2. Message IDs of 1 are temperature
				// readings from the temperature sensor; message IDs of 2 are humidity sensor
				// readings. Note that we get all the messages at once... there is a 1
				// second delay between samples,.. so the assumption is that there should
				// only be a message at most. If there are more, it is the last message
				// that will effect the status of the temperature and humidity controllers
				// as it would in reality.

				int qlen = eq.GetSize();
				for ( int i = 0; i < qlen; i++ )
				{
					Msg = eq.GetMessage();

					// Temperature reading
					if ( Msg.GetMessageId() == MessageType.TempReading.getValue() )
					{
						// if receive the current temperature update
						// update last temperature reading time.
						tempReadingTime = System.currentTimeMillis();
						// and record the messageQueue Id of temperature sensor.
						tempMsgQId = Msg.GetSenderId();
						try
						{
							CurrentTemperature = Float.valueOf(Msg.GetMessage()).floatValue();

						} // try

						catch( Exception e )
						{
							mw.WriteMessage("Error reading temperature: " + e);

						} // catch

					} // if

					// Humidity Sensor reading
					if ( Msg.GetMessageId() == MessageType.HumiReading.getValue() ) // Humidity reading
					{
						// update last humidity reading time.
						humiReadingTime = System.currentTimeMillis();
						// and record the messageQueue Id of humidity sensor.
						humiMsgQId = Msg.GetSenderId();

						System.out.println("Got humi reading");
						try
						{

							CurrentHumidity = Float.valueOf(Msg.GetMessage()).floatValue();

						} // try

						catch( Exception e )
						{
							mw.WriteMessage("Error reading humidity: " + e);

						} // catch

					} // if


					// Temperature Controller confirm message
					if ( Msg.GetMessageId() == MessageType.TempConfirm.getValue() ) //  Check Temperature Controller is alive
					{
						// update last temperature controller confirm time.
						tempCtrlMsgQId = Msg.GetSenderId();
						// and record the messageQueue Id of temperature controller.
						tempCtrlAliveTime = System.currentTimeMillis();
					}

					// Humidity Controller confirm message
					if ( Msg.GetMessageId() == MessageType.HumiConfirm.getValue() ) //  Check Humidity Controller is alive
					{
						// update last humidity controller confirm time.
						humiCtrlMsgQId = Msg.GetSenderId();
						// and record the messageQueue Id of humidity controller.
						humiCtrlAliveTime = System.currentTimeMillis();
					}

					// If the message ID == 99 then this is a signal that the simulation
					// is to end. At this point, the loop termination flag is set to
					// true and this process unregisters from the message manager.

					if ( Msg.GetMessageId() == 99 )
					{
						Done = true;

						try
						{
							em.UnRegister();

						} // try

						catch (Exception e)
						{
							mw.WriteMessage("Error unregistering: " + e);

						} // catch

						mw.WriteMessage( "\n\nSimulation Stopped. \n");

						// Get rid of the indicators. The message panel is left for the
						// user to exit so they can see the last message posted.

						hi.dispose();
						ti.dispose();

					} // if

				} // for

				mw.WriteMessage("Temperature:: " + CurrentTemperature + "F  Humidity:: " + CurrentHumidity );

				long currentTime = System.currentTimeMillis();
				//Dead detection for temperature sensor
				//if the time difference between last reading time and current time
				//exceeds the maxofflinetime(like 10 seconds), assume the Primary component has died.
				if (currentTime - tempReadingTime > maxOffLineTime){
					mw.WriteMessage("One Temperature Sensor Died.");
					try {
						// deactivate the primary temperature sensor.
						// And activate the standby temperature sensor.
						long qId = em.DeactivateMessageQueue(tempMsgQId);
						if (qId != -1){
							mw.WriteMessage("Another Temperature Sensor on queue "+qId +" has taken over successfully ! ");
						}
						tempReadingTime = currentTime;
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}

				//Dead detection for humidity sensor
				if (currentTime - humiReadingTime > maxOffLineTime){
					mw.WriteMessage("One Humidity Sensor Died.");
					try {
						long qId = em.DeactivateMessageQueue(humiMsgQId);
						if (qId != -1){
							mw.WriteMessage("Another Humidity Sensor on queue "+qId +" has taken over successfully ! ");
						}
						humiReadingTime = currentTime;
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}

				//Dead detection for temperature controller
				if (currentTime - tempCtrlAliveTime > maxOffLineTime){
					mw.WriteMessage("One Temperature Controller Died.");
					try {
						long qId = em.DeactivateMessageQueue(tempCtrlMsgQId);
						if (qId != -1){
							mw.WriteMessage("Another Temperature Controller on queue "+qId +" has taken over successfully ! ");
						}
						tempCtrlAliveTime = currentTime;
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}

				//Dead detection for humidity controller
				if (currentTime - humiCtrlAliveTime > maxOffLineTime){
					mw.WriteMessage("One Humidity Controller Died.");
					try {
						long qId = em.DeactivateMessageQueue(humiCtrlMsgQId);
						if (qId != -1){
							mw.WriteMessage("Another Humidity Controller on queue "+qId +" has taken over successfully ! ");
						}
						humiCtrlAliveTime = currentTime;
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}


				// Check temperature and effect control as necessary

				if (CurrentTemperature < TempRangeLow) // temperature is below threshhold
				{
					ti.SetLampColorAndMessage("TEMP LOW", 3);
					Heater(ON);
					Chiller(OFF);

				} else {

					if (CurrentTemperature > TempRangeHigh) // temperature is above threshhold
					{
						ti.SetLampColorAndMessage("TEMP HIGH", 3);
						Heater(OFF);
						Chiller(ON);

					} else {

						ti.SetLampColorAndMessage("TEMP OK", 1); // temperature is within threshhold
						Heater(OFF);
						Chiller(OFF);

					} // if
				} // if

				// Check humidity and effect control as necessary

				if (CurrentHumidity < HumiRangeLow)
				{
					hi.SetLampColorAndMessage("HUMI LOW", 3); // humidity is below threshhold
					Humidifier(ON);
					Dehumidifier(OFF);

				} else {

					if (CurrentHumidity > HumiRangeHigh) // humidity is above threshhold
					{
						hi.SetLampColorAndMessage("HUMI HIGH", 3);
						Humidifier(OFF);
						Dehumidifier(ON);

					} else {

						hi.SetLampColorAndMessage("HUMI OK", 1); // humidity is within threshhold
						Humidifier(OFF);
						Dehumidifier(OFF);

					} // if

				} // if

				// This delay slows down the sample rate to Delay milliseconds

				try
				{
					Thread.sleep( Delay );

				} // try

				catch( Exception e )
				{
					System.out.println( "Sleep error:: " + e );

				} // catch

			} // while

		} else {

			System.out.println("Unable to register with the message manager.\n\n" );

		} // if

	} // main

	/***************************************************************************
	 * CONCRETE METHOD:: IsRegistered
	 * Purpose: This method returns the registered status
	 *
	 * Arguments: none
	 *
	 * Returns: boolean true if registered, false if not registered
	 *
	 * Exceptions: None
	 *
	 ***************************************************************************/

	public boolean IsRegistered()
	{
		return( Registered );

	} // IsRegistered

	/***************************************************************************
	 * CONCRETE METHOD:: SetTemperatureRange
	 * Purpose: This method sets the temperature range
	 *
	 * Arguments: float lowtemp - low temperature range
	 *         float hightemp - high temperature range
	 *
	 * Returns: none
	 *
	 * Exceptions: None
	 *
	 ***************************************************************************/

	public void SetTemperatureRange(float lowtemp, float hightemp )
	{
		TempRangeHigh = hightemp;
		TempRangeLow = lowtemp;
		mw.WriteMessage( "***Temperature range changed to::" + TempRangeLow + "F - " + TempRangeHigh +"F***" );

	} // SetTemperatureRange

	/***************************************************************************
	 * CONCRETE METHOD:: SetHumidityRange
	 * Purpose: This method sets the humidity range
	 *
	 * Arguments: float lowhimi - low humidity range
	 *         float highhumi - high humidity range
	 *
	 * Returns: none
	 *
	 * Exceptions: None
	 *
	 ***************************************************************************/

	public void SetHumidityRange(float lowhumi, float highhumi )
	{
		HumiRangeHigh = highhumi;
		HumiRangeLow = lowhumi;
		mw.WriteMessage( "***Humidity range changed to::" + HumiRangeLow + "% - " + HumiRangeHigh +"%***" );

	} // SetHumidityRange

	/***************************************************************************
	 * CONCRETE METHOD:: Halt
	 * Purpose: This method posts an message that stops the environmental control
	 *        system.
	 *
	 * Arguments: none
	 *
	 * Returns: none
	 *
	 * Exceptions: Posting to message manager exception
	 *
	 ***************************************************************************/

	public void Halt()
	{
		mw.WriteMessage( "***HALT MESSAGE RECEIVED - SHUTTING DOWN SYSTEM***" );

		// Here we create the stop message.

		Message msg;

		msg = new Message( (int) 99, "XXX" );

		// Here we send the message to the message manager.

		try
		{
			em.SendMessage( msg );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending halt message:: " + e);

		} // catch

	} // Halt

	/***************************************************************************
	 * CONCRETE METHOD:: Heater
	 * Purpose: This method posts messages that will signal the temperature
	 *        controller to turn on/off the heater
	 *
	 * Arguments: boolean ON(true)/OFF(false) - indicates whether to turn the
	 *         heater on or off.
	 *
	 * Returns: none
	 *
	 * Exceptions: Posting to message manager exception
	 *
	 ***************************************************************************/

	private void Heater( boolean ON )
	{
		// Here we create the message.

		Message msg;

		if ( ON )
		{
			msg = new Message( (int) 5, "H1" );

		} else {

			msg = new Message( (int) 5, "H0" );

		} // if

		// Here we send the message to the message manager.

		try
		{
			em.SendMessage( msg );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending heater control message:: " + e);

		} // catch

	} // Heater

	/***************************************************************************
	 * CONCRETE METHOD:: Chiller
	 * Purpose: This method posts messages that will signal the temperature
	 *        controller to turn on/off the chiller
	 *
	 * Arguments: boolean ON(true)/OFF(false) - indicates whether to turn the
	 *         chiller on or off.
	 *
	 * Returns: none
	 *
	 * Exceptions: Posting to message manager exception
	 *
	 ***************************************************************************/

	private void Chiller( boolean ON )
	{
		// Here we create the message.

		Message msg;

		if ( ON )
		{
			msg = new Message( (int) 5, "C1" );

		} else {

			msg = new Message( (int) 5, "C0" );

		} // if

		// Here we send the message to the message manager.

		try
		{
			em.SendMessage( msg );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending chiller control message:: " + e);

		} // catch

	} // Chiller

	/***************************************************************************
	 * CONCRETE METHOD:: Humidifier
	 * Purpose: This method posts messages that will signal the humidity
	 *        controller to turn on/off the humidifier
	 *
	 * Arguments: boolean ON(true)/OFF(false) - indicates whether to turn the
	 *         humidifier on or off.
	 *
	 * Returns: none
	 *
	 * Exceptions: Posting to message manager exception
	 *
	 ***************************************************************************/

	private void Humidifier( boolean ON )
	{
		// Here we create the message.

		Message msg;

		if ( ON )
		{
			msg = new Message( (int) 4, "H1" );

		} else {

			msg = new Message( (int) 4, "H0" );

		} // if

		// Here we send the message to the message manager.

		try
		{
			em.SendMessage( msg );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending humidifier control message::  " + e);

		} // catch

	} // Humidifier

	/***************************************************************************
	 * CONCRETE METHOD:: Deumidifier
	 * Purpose: This method posts messages that will signal the humidity
	 *        controller to turn on/off the dehumidifier
	 *
	 * Arguments: boolean ON(true)/OFF(false) - indicates whether to turn the
	 *         dehumidifier on or off.
	 *
	 * Returns: none
	 *
	 * Exceptions: Posting to message manager exception
	 *
	 ***************************************************************************/

	private void Dehumidifier( boolean ON )
	{
		// Here we create the message.

		Message msg;

		if ( ON )
		{
			msg = new Message( (int) 4, "D1" );

		} else {

			msg = new Message( (int) 4, "D0" );

		} // if

		// Here we send the message to the message manager.

		try
		{
			em.SendMessage( msg );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending dehumidifier control message::  " + e);

		} // catch

	} // Dehumidifier

} // ECSMonitor