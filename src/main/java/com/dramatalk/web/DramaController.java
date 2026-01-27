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
    private final com.dramatalk.domain.post.PostRepository postRepository;
    

    public DramaController(DramaRepository dramaRepository,
                       RatingRepository ratingRepository,
                       com.dramatalk.domain.post.PostRepository postRepository) {
    this.dramaRepository = dramaRepository;
    this.ratingRepository = ratingRepository;
    this.postRepository = postRepository;
}


    @GetMapping
    public String list(@RequestParam(required = false) String q,
                    @RequestParam(required = false) String year,
                    @RequestParam(required = false) String genre,
                    @RequestParam(required = false) String minAvg,   // 추가
                    @RequestParam(required = false) String maxAvg,   // 추가
                    @RequestParam(required = false, defaultValue = "latest") String sort,
                    @RequestParam(required = false, defaultValue = "1") int page,   // 1부터
                    @RequestParam(required = false, defaultValue = "10") int size,
                    Model model) {

        var items = dramaRepository.searchWithAvg(q, year, genre);

        // 평균 평점 필터 (평균 평점 기준)
        var min = parseDecimal(minAvg);
        var max = parseDecimal(maxAvg);

        if (min != null || max != null) {
            items = items.stream()
                    .filter(i -> i.getAvgScore() != null) // 필터 걸면 평점 없는 건 제외
                    .filter(i -> min == null || i.getAvgScore().compareTo(min) >= 0)
                    .filter(i -> max == null || i.getAvgScore().compareTo(max) <= 0)
                    .toList();
        }

        // 정렬
        items = items.stream().sorted((a, b) -> switch (sort) {
            case "title" -> nullSafe(a.getTitle()).compareToIgnoreCase(nullSafe(b.getTitle()));
            case "year" -> nullSafe(a.getYear()).compareToIgnoreCase(nullSafe(b.getYear()));
            case "ratingDesc" -> compareScoreDesc(a, b);
            case "ratingAsc" -> compareScoreAsc(a, b);
            case "latest" -> Long.compare(b.getId(), a.getId());
            default -> Long.compare(b.getId(), a.getId());
        }).toList();

        // 페이지네이션 (in-memory)
        int total = items.size();
        int totalPages = (int) Math.ceil(total / (double) size);
        if (totalPages == 0) totalPages = 1;
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int from = (page - 1) * size;
        int to = Math.min(from + size, total);

        var pageItems = items.subList(from, to);

        // 필터 드롭다운용 유니크 값
        var years = items.stream().map(DramaListItem::getYear)
                .filter(s -> s != null && !s.isBlank()).distinct().sorted().toList();
        var genres = items.stream().map(DramaListItem::getGenre)
                .filter(s -> s != null && !s.isBlank()).distinct().sorted().toList();

        model.addAttribute("dramas", pageItems);
        model.addAttribute("years", years);
        model.addAttribute("genres", genres);

        // 평점 옵션(1.0~5.0 step 0.5)
        model.addAttribute("scoreOptions", buildScoreOptions());

        // 현재 검색값 유지
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("year", year == null ? "" : year);
        model.addAttribute("genre", genre == null ? "" : genre);
        model.addAttribute("minAvg", minAvg == null ? "" : minAvg);
        model.addAttribute("maxAvg", maxAvg == null ? "" : maxAvg);
        model.addAttribute("sort", sort);

        // 페이징 정보
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", totalPages);

        return "dramas/list";
    }

    private java.math.BigDecimal parseDecimal(String s) {
        if (s == null || s.isBlank()) return null;
        try { return new java.math.BigDecimal(s); }
        catch (Exception e) { return null; }
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

        var posts = postRepository.findByDramaIdOrderByCreatedAtDesc(id);

        Double avg = ratingRepository.findAverageScore(id);
        BigDecimal avgRounded = null;
        if (avg != null) {
            avgRounded = BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP);
        }

        model.addAttribute("drama", drama);
        model.addAttribute("ratings", ratings);
        model.addAttribute("posts", posts);
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

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var drama = dramaRepository.findById(id).orElseThrow();

        DramaForm form = new DramaForm();
        form.setId(drama.getId());
        form.setTitle(drama.getTitle());
        form.setYear(drama.getYear());
        form.setGenre(drama.getGenre());
        form.setSynopsis(drama.getSynopsis());

        model.addAttribute("dramaForm", form);
        return "dramas/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute("dramaForm") DramaForm dramaForm,
                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "dramas/edit";
        }

        var drama = dramaRepository.findById(id).orElseThrow();
        drama.setTitle(dramaForm.getTitle());
        drama.setYear(dramaForm.getYear());
        drama.setGenre(dramaForm.getGenre());
        drama.setSynopsis(dramaForm.getSynopsis());

        dramaRepository.save(drama);
        return "redirect:/dramas/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        // 평점/토론글이 FK로 연결되어 있으면 삭제가 막힐 수 있음(다음 단계에서 처리 가능)
        dramaRepository.deleteById(id);
        return "redirect:/dramas";
    }


    private List<BigDecimal> buildScoreOptions() {
        List<BigDecimal> list = new ArrayList<>();
        for (int i = 2; i <= 10; i++) { // 1.0*2=2 ~ 5.0*2=10
            list.add(BigDecimal.valueOf(i).divide(BigDecimal.valueOf(2)));
        }
        return list;
    }
}

