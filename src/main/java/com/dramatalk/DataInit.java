package com.dramatalk;

import com.dramatalk.domain.drama.Drama;
import com.dramatalk.domain.drama.DramaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInit implements CommandLineRunner {

    private final DramaRepository dramaRepository;

    public DataInit(DramaRepository dramaRepository) {
        this.dramaRepository = dramaRepository;
    }

    @Override
    public void run(String... args) {
        if (dramaRepository.count() > 0) return;

        dramaRepository.save(new Drama("샘플 드라마 A", "2003", "드라마", "샘플 줄거리 A"));
        dramaRepository.save(new Drama("샘플 드라마 B", "2016", "로맨스", "샘플 줄거리 B"));
        dramaRepository.save(new Drama("샘플 드라마 C", "2023", "미스터리", "샘플 줄거리 C"));
    }
}
