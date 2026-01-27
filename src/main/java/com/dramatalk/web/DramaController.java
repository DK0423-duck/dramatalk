package com.dramatalk.web;

import com.dramatalk.domain.drama.Drama;
import com.dramatalk.domain.drama.DramaRepository;
import com.dramatalk.domain.rating.Rating;
import com.dramatalk.domain.rating.RatingRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/dramas")
public class DramaController {

    private final DramaRepository dramaRepository;
    private final RatingRepository ratingRepository;

    public DramaController(DramaRepository dramaRepository, RatingRepository ratingRepository) {
        this.dramaRepository = dramaRepository;
        this.ratingRepository = ratingRepository;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                    @RequestParam(required = false) String year,
                    @RequestParam(required = false) String genre,
                    @RequestParam(required = false, defaultValue = "latest") String sort,
                    Model model) {

        var items = dramaRepository.searchWithAvg(q, year, genre);

        // 정렬
        items.sort((a, b) -> {
            return switch (sort) {
                case "title" -> nullSafe(a.getTitle()).compareToIgnoreCase(nullSafe(b.getTitle()));
                case "year" -> nullSafe(a.getYear()).compareToIgnoreCase(nullSafe(b.getYear()));
                case "ratingDesc" -> compareScoreDesc(a, b);
                case "ratingAsc" -> compareScoreAsc(a, b);
                case "latest" -> Long.compare(b.getId(), a.getId()); // id 큰게 최신
                default -> Long.compare(b.getId(), a.getId());
            };
        });

        // 필터 드롭다운에 쓰기(간단히 현재 목록에서 유니크 추출)
        var years = items.stream().map(DramaListItem::getYear)
                .filter(s -> s != null && !s.isBlank()).distinct().sorted().toList();
        var genres = items.stream().map(DramaListItem::getGenre)
                .filter(s -> s != null && !s.isBlank()).distinct().sorted().toList();

        model.addAttribute("dramas", items);
        model.addAttribute("years", years);
        model.addAttribute("genres", genres);

        // 현재 검색값 유지
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("year", year == null ? "" : year);
        model.addAttribute("genre", genre == null ? "" : genre);
        model.addAttribute("sort", sort);

        return "dramas/list";
    }

    private String nullSafe(String s) { return s == null ? "" : s; }

    private int compareScoreDesc(DramaListItem a, DramaListItem b) {
        var as = a.getAvgScore();
        var bs = b.getAvgScore();
        if (as == null && bs == null) return 0;
        if (as == null) return 1;   // null은 뒤로
        if (bs == null) return -1;
        return bs.compareTo(as);
    }

    private int compareScoreAsc(DramaListItem a, DramaListItem b) {
        var as = a.getAvgScore();
        var bs = b.getAvgScore();
        if (as == null && bs == null) return 0;
        if (as == null) return 1;
        if (bs == null) return -1;
        return as.compareTo(bs);
    }


    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("dramaForm", new DramaForm());
        return "dramas/new";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute DramaForm dramaForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "dramas/new";
        }

        Drama drama = new Drama(
                dramaForm.getTitle(),
                dramaForm.getYear(),
                dramaForm.getGenre(),
                dramaForm.getSynopsis()
        );
        dramaRepository.save(drama);
        return "redirect:/dramas/" + drama.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Drama drama = dramaRepository.findById(id).orElseThrow();
        List<Rating> ratings = ratingRepository.findByDramaIdOrderByCreatedAtDesc(id);

        Double avg = ratingRepository.findAverageScore(id);
        BigDecimal avgRounded = null;
        if (avg != null) {
            avgRounded = BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP);
        }

        model.addAttribute("drama", drama);
        model.addAttribute("ratings", ratings);
        model.addAttribute("avgScore", avgRounded);

        model.addAttribute("ratingForm", new RatingForm());
        model.addAttribute("scoreOptions", buildScoreOptions()); // 1.0~5.0, 0.5
        return "dramas/detail";
    }

    @PostMapping("/{id}/ratings")
    public String addRating(@PathVariable Long id,
                            @Valid @ModelAttribute RatingForm ratingForm,
                            BindingResult bindingResult,
                            Model model) {
        Drama drama = dramaRepository.findById(id).orElseThrow();

        if (bindingResult.hasErrors()) {
            // detail 페이지 다시 렌더링에 필요한 데이터 채우기
            List<Rating> ratings = ratingRepository.findByDramaIdOrderByCreatedAtDesc(id);
            Double avg = ratingRepository.findAverageScore(id);

            BigDecimal avgRounded = null;
            if (avg != null) {
                avgRounded = BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP);
            }

            model.addAttribute("drama", drama);
            model.addAttribute("ratings", ratings);
            model.addAttribute("avgScore", avgRounded);
            model.addAttribute("scoreOptions", buildScoreOptions());
            return "dramas/detail";
        }

        Rating rating = new Rating(drama, ratingForm.getScore(), ratingForm.getComment());
        ratingRepository.save(rating);
        return "redirect:/dramas/" + id;
    }

    private List<BigDecimal> buildScoreOptions() {
        List<BigDecimal> list = new ArrayList<>();
        for (int i = 2; i <= 10; i++) { // 1.0*2=2 ~ 5.0*2=10
            list.add(BigDecimal.valueOf(i).divide(BigDecimal.valueOf(2)));
        }
        return list;
    }
}

