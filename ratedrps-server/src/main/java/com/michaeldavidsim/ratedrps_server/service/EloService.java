package com.michaeldavidsim.ratedrps_server.service;

import org.springframework.stereotype.Service;

@Service
public class EloService {

    private static final int DEFAULT_K_FACTOR = 32;

    // score: player 1 = 1.0, player 2 = 0.0, draw = 0.5
    public int[] calculateNewRatings(int ratingA, int ratingB, double scoreA) {
        double expectedA = 1.0 / (1.0 + Math.pow(10, (ratingB - ratingA) / 400.0));
        double expectedB = 1.0 - expectedA;

        double scoreB = 1.0 - scoreA;

        int newRatingA = (int) Math.round(ratingA + DEFAULT_K_FACTOR * (scoreA - expectedA));
        int newRatingB = (int) Math.round(ratingB + DEFAULT_K_FACTOR * (scoreB - expectedB));

        return new int[]{newRatingA, newRatingB};
    }
}
