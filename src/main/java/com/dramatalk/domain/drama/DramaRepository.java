package com.dramatalk.domain.drama;

import com.dramatalk.web.DramaListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DramaRepository extends JpaRepository<Drama, Long> {

    @Query("""
        select new com.dramatalk.web.DramaListItem(
            d.id,
            d.title,
            d.year,
            d.genre,
            cast(avg(r.score) as java.math.BigDecimal)
        )
        from Drama d
        left join com.dramatalk.domain.rating.Rating r on r.drama.id = d.id
        where (:q is null or :q = '' or lower(d.title) like lower(concat('%', :q, '%')))
          and (:year is null or :year = '' or d.year = :year)
          and (:genre is null or :genre = '' or d.genre = :genre)
        group by d.id, d.title, d.year, d.genre
        """)
    List<DramaListItem> searchWithAvg(@Param("q") String q,
                                     @Param("year") String year,
                                     @Param("genre") String genre);
}
