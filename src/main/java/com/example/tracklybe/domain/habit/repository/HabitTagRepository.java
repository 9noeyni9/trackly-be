package com.example.tracklybe.domain.habit.repository;

import com.example.tracklybe.domain.habit.entity.Habit;
import com.example.tracklybe.domain.habit.entity.HabitTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface HabitTagRepository extends JpaRepository<HabitTag, Long> {

    @Query("""
            select t.name
            from HabitTag ht
            join ht.tag t
            where ht.habit.habitId = :habitId
            """)
    Set<String> findTagNamesByHabitId(Long habitId);

    @Query("""
        select ht.habit.habitId, t.name
        from HabitTag ht
        join ht.tag t
        where ht.habit.habitId in :habitIds
    """)
    List<Object[]> findHabitIdAndTagNameByHabitIds(@Param("habitIds") List<Long> habitIds);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
                delete from HabitTag ht
                where ht.habit.habitId = :habitId
                  and ht.tag.name in :tagNames
            """)
    int deleteByHabitIdAndTagNames(@Param("habitId") Long habitId,
                                   @Param("tagNames") Collection<String> tagNames);

    void deleteByHabit(Habit habit);
}
