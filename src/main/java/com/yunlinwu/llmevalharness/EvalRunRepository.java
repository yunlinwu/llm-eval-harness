package com.yunlinwu.llmevalharness;

import org.springframework.data.jpa.repository.JpaRepository;

/** Free CRUD for EvalRun — the same interface-with-no-code trick as EvalRepository. */
public interface EvalRunRepository extends JpaRepository<EvalRun, Long> {
}
