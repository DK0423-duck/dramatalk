package com.dramatalk.domain.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Post 상세에서 댓글 목록 조회용 (PostController에서 사용)
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);

    // Drama 삭제 시: 해당 드라마에 속한 모든 댓글 삭제용
    @Transactional
    void deleteByPostDramaId(Long dramaId);
}
