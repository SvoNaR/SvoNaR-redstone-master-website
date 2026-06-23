package ru.redstonemaster.web.news;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsPostRepository extends JpaRepository<NewsPost, Long> {

	List<NewsPost> findAllByOrderByCreatedAtDesc();
}
