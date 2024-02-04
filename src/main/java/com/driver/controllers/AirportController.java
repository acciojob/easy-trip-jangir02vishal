// AirportController.java
package com.driver.controllers;

import com.driver.model.Airport;
import com.driver.model.City;
import com.driver.model.Flight;
import com.driver.model.Passenger;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class AirportController {
    private final Map<String, Airport> airportMap = new HashMap<>();
    private final List<Flight> flights = new ArrayList<>();
    private final Map<Integer, Passenger> passengers = new HashMap<>();
    private final Map<Integer, List<Integer>> flightBookings = new HashMap<>();

    @PostMapping("/add_airport")
    public String addAirport(@RequestBody Airport airport) {
        airportMap.put(airport.getAirportName(), airport);
        return "SUCCESS";
    }

    @GetMapping("/get-largest-aiport")
    public String getLargestAirportName() {
        String largestAirport = airportMap.entrySet().stream()
                .max(Comparator.comparingInt(entry -> entry.getValue().getNoOfTerminals()))
                .map(Map.Entry::getKey)
                .orElse(null);

        if (largestAirport == null) {
            return null;
        }

        return airportMap.entrySet().stream()
                .filter(entry -> entry.getValue().getNoOfTerminals() == airportMap.get(largestAirport).getNoOfTerminals())
                .map(Map.Entry::getKey)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    @GetMapping("/get-shortest-time-travel-between-cities")
    public double getShortestDurationOfPossibleBetweenTwoCities(@RequestParam("fromCity") City fromCity,
                                                                @RequestParam("toCity") City toCity) {
        Optional<Flight> shortestFlight = flights.stream()
                .filter(flight -> flight.getFromCity() == fromCity && flight.getToCity() == toCity)
                .min(Comparator.comparingDouble(Flight::getDuration));

        return shortestFlight.map(Flight::getDuration).orElse(-1.0);
    }

    @GetMapping("/get-number-of-people-on-airport-on/{date}")
    public int getNumberOfPeopleOn(@PathVariable("date") Date date, @RequestParam("airportName") String airportName) {
        return (int) flightBookings.entrySet().stream()
                .filter(entry -> flights.stream()
                        .anyMatch(flight -> flight.getFlightId() == entry.getKey() && flight.getFlightDate().equals(date)))
                .flatMap(entry -> entry.getValue().stream())
                .distinct()
                .count();
    }

    @GetMapping("/calculate-fare")
    public int calculateFlightFare(@RequestParam("flightId") Integer flightId) {
        return 3000 + flightBookings.getOrDefault(flightId, Collections.emptyList()).size() * 50;
    }

    @PostMapping("/book-a-ticket")
    public String bookATicket(@RequestParam("flightId") Integer flightId, @RequestParam("passengerId") Integer passengerId) {
        if (!flights.stream().anyMatch(flight -> flight.getFlightId() == flightId) ||
                !passengers.containsKey(passengerId) ||
                flightBookings.getOrDefault(flightId, Collections.emptyList()).size() >=
                        flights.stream().filter(flight -> flight.getFlightId() == flightId).findFirst()
                                .map(Flight::getMaxCapacity).orElse(0) ||
                flightBookings.getOrDefault(flightId, Collections.emptyList()).contains(passengerId)) {
            return "FAILURE";
        }

        flightBookings.computeIfAbsent(flightId, k -> new ArrayList<>()).add(passengerId);
        return "SUCCESS";
    }

    @PutMapping("/cancel-a-ticket")
    public String cancelATicket(@RequestParam("flightId") Integer flightId, @RequestParam("passengerId") Integer passengerId) {
        if (!flights.stream().anyMatch(flight -> flight.getFlightId() == flightId) ||
                !passengers.containsKey(passengerId) ||
                !flightBookings.containsKey(flightId) ||
                !flightBookings.get(flightId).contains(passengerId)) {
            return "FAILURE";
        }

        flightBookings.get(flightId).remove(passengerId);
        return "SUCCESS";
    }

    @GetMapping("/get-count-of-bookings-done-by-a-passenger/{passengerId}")
    public int countOfBookingsDoneByPassengerAllCombined(@PathVariable("passengerId") Integer passengerId) {
        return (int) flightBookings.entrySet().stream()
                .filter(entry -> entry.getValue().contains(passengerId))
                .count();
    }

    @PostMapping("/add-flight")
    public String addFlight(@RequestBody Flight flight) {
        flights.add(flight);
        return "SUCCESS";
    }

    @GetMapping("/get-aiportName-from-flight-takeoff/{flightId}")
    public String getAirportNameFromFlightId(@PathVariable("flightId") Integer flightId) {
        return flights.stream()
                .filter(flight -> flight.getFlightId() == flightId)
                .findFirst()
                .map(flight -> airportMap.get(flight.getFromCity().name()).getAirportName())
                .orElse(null);
    }

    @GetMapping("/calculate-revenue-collected/{flightId}")
    public int calculateRevenueOfAFlight(@PathVariable("flightId") Integer flightId) {
        return flightBookings.getOrDefault(flightId, Collections.emptyList()).size() * 50;
    }

    @PostMapping("/add-passenger")
    public String addPassenger(@RequestBody Passenger passenger) {
        passengers.put(passenger.getPassengerId(), passenger);
        return "SUCCESS";
    }
}
