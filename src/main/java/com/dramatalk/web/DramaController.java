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
    public String list(Model model) {
        List<Drama> dramas = dramaRepository.findAll();
        model.addAttribute("dramas", dramas);
        return "dramas/list";
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

