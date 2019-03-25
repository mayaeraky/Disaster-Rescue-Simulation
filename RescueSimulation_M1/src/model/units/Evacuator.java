package model.units;

import model.events.WorldListener;
import simulation.Address;

public class Evacuator extends PoliceUnit {

	public Evacuator(String unitID, Address location, int stepsPerCycle, WorldListener w,int maxCapacity) {

		super(unitID, location, stepsPerCycle,maxCapacity, w);

	}

}
