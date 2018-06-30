package de.elliepotato.steve.status;

/**
 * @author Ellie for VentureNode LLC
 * at 22/05/2018
 */
public interface StatusFetcher<T> {

    void fetch();

    T getLastFetch();

    long lastFetch();

}
