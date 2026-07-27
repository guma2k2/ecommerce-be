//package com.yas.system.catalog.internal.repository;
//
//import com.yas.system.catalog.internal.entity.Review;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface ReviewRepository extends JpaRepository<Review, Long> {
//
//
//
//    @Query("""
//        select r
//        from Review r
//        join fetch r.productVariant pv
//        join fetch pv.product p
//        where p.id = :productId and r.ratingStar in :ratings
//    """)
//    Page<Review> findByProductIdAndRatingStar(Long productId, Pageable pageable, List<Integer> ratings);
//
//
//    @Query("""
//        select r
//        from Review r
//        join fetch r.productVariant pv
//        join fetch pv.product p
//        where p.id = :productId
//    """)
//    Page<Review> findByProductId(Long productId, Pageable pageable);
//
//}
//
//
