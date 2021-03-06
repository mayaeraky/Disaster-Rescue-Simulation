package simulation;

import java.io.BufferedReader;
import model.infrastructure.*;
import model.people.*;
import java.io.FileReader;
import java.util.ArrayList;

import model.disasters.Disaster;
import model.disasters.Fire;
import model.disasters.GasLeak;
import model.disasters.Infection;
import model.disasters.Injury;
import model.events.SOSListener;
import model.events.WorldListener;
import model.infrastructure.ResidentialBuilding;
import model.people.Citizen;
import model.units.Ambulance;
import model.units.DiseaseControlUnit;
import model.units.Evacuator;
import model.units.FireTruck;
import model.units.GasControlUnit;
import model.units.Unit;
import model.units.UnitState;
import model.disasters.*;
public class Simulator implements WorldListener{

	private int currentCycle;
	private ArrayList<ResidentialBuilding> buildings;
	private ArrayList<Citizen> citizens;
	private ArrayList<Unit> emergencyUnits;
	private ArrayList<Disaster> plannedDisasters;
	private ArrayList<Disaster> executedDisasters;
	private Address[][] world;
	private  SOSListener emergencyService;

	public Simulator(SOSListener s) throws Exception {

		buildings = new ArrayList<ResidentialBuilding>();
		citizens = new ArrayList<Citizen>();
		emergencyUnits = new ArrayList<Unit>();
		plannedDisasters = new ArrayList<Disaster>();
		executedDisasters = new ArrayList<Disaster>();
		this.emergencyService=s;

		world = new Address[10][10];
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				world[i][j] = new Address(i, j);
			}
		}

		loadUnits("units.csv");
		loadBuildings("buildings.csv");
		loadCitizens("citizens.csv");
		loadDisasters("disasters.csv");

		for (int i = 0; i < buildings.size(); i++) {

			ResidentialBuilding building = buildings.get(i);
			for (int j = 0; j < citizens.size(); j++) {

				Citizen citizen = citizens.get(j);
				if (citizen.getLocation() == building.getLocation())
					building.getOccupants().add(citizen);

			}
		}
		for(int i=0;i<this.citizens.size();i++){
			this.citizens.get(i).setEmergencyService(emergencyService);
		}
		for(int i=0;i<this.buildings.size();i++){
			this.buildings.get(i).setEmergencyService(emergencyService);
		}
	}

	private void loadUnits(String path) throws Exception {

		BufferedReader br = new BufferedReader(new FileReader(path));
		String line = br.readLine();

		while (line != null) {

			String[] info = line.split(",");
			String id = info[1];
			int steps = Integer.parseInt(info[2]);

			switch (info[0]) {

			case "AMB":
				emergencyUnits.add(new Ambulance(id, world[0][0], steps,this));
				break;

			case "DCU":
				emergencyUnits.add(new DiseaseControlUnit(id, world[0][0], steps,this));
				break;

			case "EVC":
				emergencyUnits.add(new Evacuator(id, world[0][0], steps,this, Integer.parseInt(info[3])));
				break;

			case "FTK":
				emergencyUnits.add(new FireTruck(id, world[0][0], steps,this));
				break;

			case "GCU":
				emergencyUnits.add(new GasControlUnit(id, world[0][0], steps,this));
				break;

			}

			line = br.readLine();
		}

		br.close();
	}

	private void loadBuildings(String path) throws Exception {

		BufferedReader br = new BufferedReader(new FileReader(path));
		String line = br.readLine();

		while (line != null) {

			String[] info = line.split(",");
			int x = Integer.parseInt(info[0]);
			int y = Integer.parseInt(info[1]);

			buildings.add(new ResidentialBuilding(world[x][y]));

			line = br.readLine();

		}
		br.close();
	}

	private void loadCitizens(String path) throws Exception {

		BufferedReader br = new BufferedReader(new FileReader(path));
		String line = br.readLine();

		while (line != null) {

			String[] info = line.split(",");
			int x = Integer.parseInt(info[0]);
			int y = Integer.parseInt(info[1]);
			String id = info[2];
			String name = info[3];
			int age = Integer.parseInt(info[4]);

			citizens.add(new Citizen(world[x][y], id, name, age,this));

			line = br.readLine();

		}
		br.close();
	}

	private void loadDisasters(String path) throws Exception {

		BufferedReader br = new BufferedReader(new FileReader(path));
		String line = br.readLine();

		while (line != null) {

			String[] info = line.split(",");
			int startCycle = Integer.parseInt(info[0]);
			ResidentialBuilding building = null;
			Citizen citizen = null;

			if (info.length == 3)
				citizen = getCitizenByID(info[2]);
			else {

				int x = Integer.parseInt(info[2]);
				int y = Integer.parseInt(info[3]);
				building = getBuildingByLocation(world[x][y]);

			}

			switch (info[1]) {

			case "INJ":
				plannedDisasters.add(new Injury(startCycle, citizen));
				break;

			case "INF":
				plannedDisasters.add(new Infection(startCycle, citizen));
				break;

			case "FIR":
				plannedDisasters.add(new Fire(startCycle, building));
				break;

			case "GLK":
				plannedDisasters.add(new GasLeak(startCycle, building));
				break;
			}

			line = br.readLine();
		}
		br.close();
	}

	private Citizen getCitizenByID(String id) {

		for (int i = 0; i < citizens.size(); i++) {
			if (citizens.get(i).getNationalID().equals(id))
				return citizens.get(i);
		}

		return null;
	}

	private ResidentialBuilding getBuildingByLocation(Address location) {

		for (int i = 0; i < buildings.size(); i++) {
			if (buildings.get(i).getLocation() == location)
				return buildings.get(i);
		}

		return null;
	}
	public void setEmergencyService(SOSListener emergencyService) {
		this.emergencyService = emergencyService;
	}
	public int calculateCasualties(){
		int x=0;
		for(int i=0;i<this.citizens.size();i++){
			if(this.citizens.get(i).getState()==CitizenState.DECEASED)
				x++;
		}
		return x;
	}
	public void assignAddress(Simulatable sim, int x, int y){
		   if (sim instanceof Citizen){
			((Citizen)sim).setLocation(world[x][y]);
		}
		   if (sim instanceof Unit){
				((Unit)sim).setLocation(world[x][y]);
			}

		}
		public ArrayList<Unit> getEmergencyUnits() {
			return emergencyUnits;
		}
		
	public boolean checkGameOver(){
		if(this.plannedDisasters.size()!=0)
			return false;
		for(int i=0;i<this.executedDisasters.size();i++){
			if(this.executedDisasters.get(i).isActive()){
				if(this.executedDisasters.get(i).getTarget() instanceof Citizen){
					if(((Citizen)this.executedDisasters.get(i).getTarget()).getState()!=CitizenState.DECEASED){
						return false;
					}}
				else if(((ResidentialBuilding)this.executedDisasters.get(i).getTarget()).getStructuralIntegrity()!=0)
					return false;
					}
				}
		for(int i=0;i<this.emergencyUnits.size();i++){
			if(this.emergencyUnits.get(i).getState()!=UnitState.IDLE)
				return false;
			}
		return true;
		}
	public void nextCycle(){
		this.currentCycle++;
		
		//for(int i=0;i<this.plannedDisasters.size();i++){
			//if(this.plannedDisasters.get(i).getStartCycle()==this.currentCycle){
				//if(getDisasterType(this.plannedDisasters.get(i))!=null){
				//this.executedDisasters.add(getDisasterType(this.plannedDisasters.get(i)));
				//this.plannedDisasters.set(i, getDisasterType(this.plannedDisasters.get(i)));
				//this.plannedDisasters.get(i).strike();	
				//this.plannedDisasters.remove(i);
			//}else this.plannedDisasters.get(i).strike();}
		//}
		//for(int i=0;i<this.emergencyUnits.size();i++){
			//this.emergencyUnits.get(i).cycleStep();
		//}
		for(int i=0;i<this.plannedDisasters.size();i++){
			if(this.plannedDisasters.get(i).getStartCycle()==this.currentCycle) {
			if(this.plannedDisasters.get(i).getTarget().getDisaster()==null){
				this.executedDisasters.add(this.plannedDisasters.get(i));
			}
			else if(this.plannedDisasters.get(i) instanceof Fire){
				if(((ResidentialBuilding)this.plannedDisasters.get(i).getTarget()).getGasLevel()==0){
					this.executedDisasters.add(this.plannedDisasters.get(i));
				}
				else if(((ResidentialBuilding)this.plannedDisasters.get(i).getTarget()).getGasLevel()>0 &&((ResidentialBuilding)this.plannedDisasters.get(i).getTarget()).getGasLevel()<70 ){
					this.plannedDisasters.set(i, new Collapse(this.currentCycle,((ResidentialBuilding)this.plannedDisasters.get(i).getTarget())));
					this.executedDisasters.add(this.plannedDisasters.get(i));
				}
				else {
					((ResidentialBuilding)this.plannedDisasters.get(i).getTarget()).setStructuralIntegrity(0);
				}
			}
			else if(this.plannedDisasters.get(i) instanceof GasLeak && ((ResidentialBuilding)this.plannedDisasters.get(i).getTarget()).getDisaster() instanceof Fire){
				this.plannedDisasters.set(i, new Collapse(this.currentCycle,((ResidentialBuilding)this.plannedDisasters.get(i).getTarget())));
				this.executedDisasters.add(this.plannedDisasters.get(i));
			}
		    if(this.plannedDisasters.get(i) instanceof Collapse){
				for(int j=0;j<this.executedDisasters.size();j++){
					if(this.executedDisasters.get(j).getTarget().equals(this.plannedDisasters.get(i).getTarget())) {
						this.executedDisasters.remove(j);
					}}
				if(this.plannedDisasters.get(i).getTarget().getDisaster()!=null){
					this.executedDisasters.add(this.plannedDisasters.get(i));
				}
		    }
				this.plannedDisasters.get(i).strike();
				this.plannedDisasters.remove(i);
				
		    }
		    for(int j=0;j<this.buildings.size();j++){
		    	if(this.buildings.get(j).getFireDamage()==100){
		    		Disaster d=new Collapse(this.currentCycle,this.buildings.get(j));
		    		d.strike();
		    		this.executedDisasters.add(d);
		    	}
		    }
		    }
		
		for(int i=0;i<this.emergencyUnits.size();i++){
			this.emergencyUnits.get(i).cycleStep();
		}
		
	   		for(int i=0;i<this.executedDisasters.size();i++){
			if(this.executedDisasters.get(i).isActive() && this.executedDisasters.get(i).getStartCycle()!=this.currentCycle)
				this.executedDisasters.get(i).cycleStep();
		}
		for(int i=0;i<this.buildings.size();i++)
			this.buildings.get(i).cycleStep();
		for(int  i=0;i<this.citizens.size();i++)
			this.citizens.get(i).cycleStep();
	}
	
	
	
	
	
	
	public Disaster getDisasterType(Disaster planned){
		if(planned instanceof Infection) return new Infection(this.currentCycle,((Citizen)planned.getTarget()));
		if(planned instanceof Injury) return new Injury(this.currentCycle,((Citizen)planned.getTarget()));
		if(planned.getTarget() instanceof ResidentialBuilding && ((ResidentialBuilding)planned.getTarget()).getFireDamage()==100){
			((ResidentialBuilding)planned.getTarget()).setFireDamage(0);
			//wording ghareeb gdan
			return new Collapse(this.currentCycle,((ResidentialBuilding)planned.getTarget()));
		}
		if(planned instanceof Collapse){
			((ResidentialBuilding)planned.getTarget()).setFireDamage(0);
			//wording ghareeb gdan
			return new Collapse(this.currentCycle,((ResidentialBuilding)planned.getTarget()));
		}
		if(planned instanceof Fire){
			if(((ResidentialBuilding)planned.getTarget()).getGasLevel()==0){
				return new Fire(this.currentCycle,((ResidentialBuilding)planned.getTarget()));
			}
			if(((ResidentialBuilding)planned.getTarget()).getGasLevel()>0 && ((ResidentialBuilding)planned.getTarget()).getGasLevel()<70){
				return new Collapse(this.currentCycle,((ResidentialBuilding)planned.getTarget()));
			}
			if(((ResidentialBuilding)planned.getTarget()).getGasLevel()>=70){
				((ResidentialBuilding)planned.getTarget()).setStructuralIntegrity(0);
				return null;
			}
		}
		if(planned.getTarget() instanceof ResidentialBuilding)
		return new Collapse(this.currentCycle,((ResidentialBuilding)planned.getTarget()));
		return null;
	}


}
