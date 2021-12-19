package org.jason.flightgear.aircraft.alouette3;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.jason.flightgear.aircraft.FlightGearAircraft;
import org.jason.flightgear.connection.sockets.FlightGearInputConnection;

public class Alouette3 extends FlightGearAircraft {

	public Alouette3() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String readTelemetryRaw() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void writeControlInput(LinkedHashMap<String, String> inputHash,
			FlightGearInputConnection socketConnection) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeConsumeablesInput(LinkedHashMap<String, String> inputHash) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeControlInput(LinkedHashMap<String, String> inputHash) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeFdmInput(LinkedHashMap<String, String> inputHash) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeOrientationInput(LinkedHashMap<String, String> inputHash) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writePositionInput(LinkedHashMap<String, String> inputHash) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeSimFreezeInput(LinkedHashMap<String, String> inputHash) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeSimSpeedupInput(LinkedHashMap<String, String> inputHash) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeSystemInput(LinkedHashMap<String, String> inputHash) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeVelocitiesInput(LinkedHashMap<String, String> inputHash) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public double getFuelLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getFuelTankCapacity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEngineRunning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setLatitude(double targetLatitude) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLongitude(double targetLongitude) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAltitude(double targetAltitude) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAirSpeed(double targetSpeed) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVerticalSpeed(double targetSpeed) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void refillFuel() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setParkingBrake(boolean brakeEnabled) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeEnginesInput(LinkedHashMap<String, String> inputHash) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void writeSimTimeInput(LinkedHashMap<String, String> inputHash) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
