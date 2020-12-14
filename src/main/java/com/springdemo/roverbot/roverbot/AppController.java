package com.springdemo.roverbot.roverbot;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.springframework.boot.json.GsonJsonParser;
import org.springframework.core.Conventions;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping(value = "/roverbot")
public class AppController {

	public AppService appService = new AppService();
	RoverBot rb = new RoverBot();

	@RequestMapping(value = "/get-position", method = RequestMethod.GET)
	public ResponseEntity<PositionClass> getPosition() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		PositionClass position = appService.getPosition(); // appService.getPosition();
		if (position == null) return new ResponseEntity<PositionClass>(null, headers, HttpStatus.NOT_FOUND);
		
		return new ResponseEntity<PositionClass>(position, headers, HttpStatus.OK);
	}

	@RequestMapping(value = "/calculate-position", method = RequestMethod.POST)
	public PositionClass calculatePosition(@RequestBody CalculationInput ci) {
		// sets initial position
		// rb.getInstance();
		rb.setPosition(ci.Position.getDirection(), ci.Position.getX(), ci.Position.getY());

		// Orders the moves by OrderID
		Map<Integer, MoveClass> moveMap = new Hashtable<Integer, MoveClass>();
		for (MoveClass m : ci.Move) {
			moveMap.put(Integer.parseInt(m.getO()), m);
		}

		// Updates the position
		for (int i = 1; i <= moveMap.size(); i++) {
			MoveClass m = moveMap.get(i);
			if (m != null) {
				// update direction (eg: "L" - 90 degree)
				String dir = "L";
				int dirDegree = m.getL();
				if (dirDegree < 0) {
					dir = "R";
					dirDegree = m.getR();
				}
				// update position (eg: "F" - 10 points)
				String moveDir = "F";
				int movePoints = m.getF();
				if (movePoints < 0) {
					moveDir = "B";
					movePoints = m.getB();
				}
				rb.updateDirection(dir, dirDegree);
				rb.updatePosition(moveDir, movePoints);
			}
		}

		appService.setPosition(rb.getPosition());
		return appService.getPosition();
	}
}

class RoverBot {

	private String[] directions;
	public PositionClass pc;
	
	RoverBot() {
	}

	public PositionClass getPosition() {
		return pc;
	}

	public void setPosition(String direction, int X, int Y) {
		directions = new String[] { "N", "E", "S", "W" };
		pc = new PositionClass();
		
		pc.setDirection(direction.toUpperCase());
		pc.setX(X);
		pc.setY(Y);
//		System.out.println(pc.getDirection() + "-" + pc.getX() + "-" + pc.getY());
	}

	public void updatePosition(String moveDirection, int movePoints) {

		int currentX = pc.getX();
		int currentY = pc.getY();
//		System.out.println(cureentX + "-" + currentY);

		if (pc.getDirection() == "N") {
			// update Y
			if (moveDirection == "F")
				pc.setY(currentY + movePoints);
			else
				pc.setY(currentY - movePoints);
		} else if (pc.getDirection() == "E") {
			// update X
			if (moveDirection == "F")
				pc.setX(currentX + movePoints);
			else
				pc.setX(currentX - movePoints);
		} else if (pc.getDirection() == "S") {
			// update Y
			if (moveDirection == "F")
				pc.setY(currentY - movePoints);
			else
				pc.setY(currentY + movePoints);
		} else { // W
			// update X
			if (moveDirection == "F")
				pc.setX(currentX - movePoints);
			else
				pc.setX(currentX + movePoints);
		}

//		System.out.println(pc.getX() + " :" + pc.getY());
	}

	public void updateDirection(String currentTurnDirection, int degree) {
		String currentDirection = pc.getDirection();
		System.out.println("Updating direction-" + currentDirection+"-"+directions.length+"-"+currentTurnDirection+"-"+degree);
		int directionIndex = 0;

		for (int ix = 0; ix < directions.length; ix++) {
			System.out.println(directions[ix]+"-"+ix);
			if (directions[ix].trim().equals(currentDirection.trim())) {
				directionIndex = ix;
				System.out.println("SELECTED");
				break;
			}
		}

		int degreeIndex = degree / 90; // Since 90 degree is the difference
		// update only if degree index is other than 0 and 4
		if (degreeIndex != 0 && degreeIndex != 4) {
			int index = 0;
			if (currentTurnDirection == "L") {
				// Left direction
				index = (directionIndex - degreeIndex) % directions.length;
				if (index < 0)
					index = directions.length + index;
			} else {
				// Right direction
				index = (directionIndex + degreeIndex) % directions.length;
			}
			String movedDirection = directions[index];
			System.out.println(currentDirection + "-" + directionIndex + "-" + degreeIndex + "-" + movedDirection);
			pc.setDirection(movedDirection);
		}
	}
}

class CalculationInput {
	PositionClass Position = new PositionClass();
	List<MoveClass> Move = new ArrayList<MoveClass>();

	public PositionClass getPosition() {
		return Position;
	}

	public void setPosition(PositionClass position) {
		Position = position;
	}

	public List<MoveClass> getMove() {
		return Move;
	}

	public void setMove(List<MoveClass> move) {
		Move = move;
	}

}

class PositionClass {
//	Directions Direction = Directions.NORTH;
	String Direction;
	int X = -1;
	int Y = -1;

	public String getDirection() {
		return Direction;
	}

	public void setDirection(String direction) {
		Direction = direction;
	}

	public int getX() {
		return X;
	}

	public void setX(int x) {
		X = x;
	}

	public int getY() {
		return Y;
	}

	public void setY(int y) {
		Y = y;
	}

}

class MoveClass {
	String O;
	int L = -1;
	int R = -1;
	int F = -1;
	int B = -1;

	public String getO() {
		return O;
	}

	public void setO(String o) {
		O = o;
	}

	public int getL() {
		return L;
	}

	public void setL(int l) {
		L = l;
	}

	public int getR() {
		return R;
	}

	public void setR(int r) {
		R = r;
	}

	public int getF() {
		return F;
	}

	public void setF(int f) {
		F = f;
	}

	public int getB() {
		return B;
	}

	public void setB(int b) {
		B = b;
	}

}

enum Directions {
	NORTH, EAST, SOUTH, WEST
}