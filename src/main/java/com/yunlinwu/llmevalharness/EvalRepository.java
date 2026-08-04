package com.yunlinwu.llmevalharness;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * This is where Spring Data JPA does the biggest single trick in the framework.
 *
 * We define an INTERFACE. We write ZERO methods. We do not implement it.
 *
 * At startup, Spring sees this interface and generates a working implementation
 * for us — one that can save(entity), findById(id), findAll(), delete(entity),
 * count(), and about a dozen other things. All backed by our H2 database.
 *
 * The magic is in the parameterization: {@code JpaRepository<Eval, Long>}
 * tells Spring "this repository manages Eval entities, whose ID type is Long."
 * That's enough for Spring to wire up every CRUD operation.
 *
 * The 20/80 payoff: for 80% of database work, you never write SQL. When you
 * need something exotic (a custom query, a join, a bulk update), you add a
 * method signature here with {@code @Query} — but for standard CRUD, this
 * one-line interface is all you need.
 */
public interface EvalRepository extends JpaRepository<Eval, Long> {
}
