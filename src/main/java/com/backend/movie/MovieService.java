package com.backend.movie;

import com.backend.Error;
import com.backend.category.CategoryEntity;
import com.backend.movie.dto.CreateMovieDTO;
import com.backend.movie.dto.MovieDetailDTO;
import com.backend.movie.dto.MovieFilterDTO;
import com.backend.movie.dto.UpdateMovieDTO;
import com.backend.security.CurrentUser;
import com.backend.util.StringUtils;
import com.mongodb.BasicDBObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;


import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class MovieService {
    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Secured("ROLE_ADMIN")
    public MovieEntity createMovie(CreateMovieDTO input){
        MovieEntity toCreateMovie = new MovieEntity();

        BeanUtils.copyProperties(input, toCreateMovie);
        toCreateMovie.setCreatedAt(new Date().getTime());
        toCreateMovie.setNameVnR(StringUtils.convertToRaw(input.getNameVn()));
        toCreateMovie.setSummaryR(StringUtils.convertToRaw(input.getSummary()));

        return movieRepository.save(toCreateMovie);
    }

    @Secured("ROLE_ADMIN")
    public MovieEntity updateMovie(UpdateMovieDTO input){
        MovieEntity existed = movieRepository.findById(input.getId()).orElse(null);
        if(existed == null) throw Error.NotFoundError("Movie");

        BeanUtils.copyProperties(input, existed);
        existed.setNameVnR(StringUtils.convertToRaw(input.getNameVn()));
        existed.setSummaryR(StringUtils.convertToRaw(input.getSummary()));

        return movieRepository.save(existed);
    }

    @Secured("ROLE_ADMIN")
    public MovieEntity deleteMovie(String id){
        MovieEntity existed = movieRepository.findById(id).orElse(null);
        if(existed == null) throw Error.NotFoundError("Movie");

        movieRepository.delete(existed);

        return existed;
    }

    public List<MovieEntity> filterMovie(MovieFilterDTO input){
        Query query = new Query();

        if(input.getKeyword() != null){
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("nameVnR").regex(input.getKeyword(),"gmi"),
                    Criteria.where("summaryR").regex(input.getKeyword(), "gmi"),
                    Criteria.where("nameEn").regex(input.getKeyword(),"gmi")
            ));
        }

        if(input.getCategory() != null){
            query.addCriteria(Criteria.where("categories").is(input.getCategory()));
        }

        if(input.getYear() != 0){
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, input.getYear());

            calendar.set(Calendar.MONTH, 1);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            long startYear = calendar.getTimeInMillis();

            calendar.set(Calendar.MONTH, 12);
            calendar.set(Calendar.DAY_OF_MONTH, 30);
            long endYear = calendar.getTimeInMillis();

            query.addCriteria(Criteria.where("releaseDate").gte(startYear).lte(endYear));
        }

        if(input.getLastRelease() != null && input.getLastRelease().equals("false"))
            query.with(Sort.by(Sort.Direction.ASC, "releaseDate"));
        else // Sort by default
            query.with(Sort.by(Sort.Direction.DESC, "releaseDate"));

        if(input.getSkip() != 0){
            query.skip(input.getSkip());
        }

        if(input.getLimit() != 0){
            query.limit(input.getLimit());
        }

        return mongoTemplate.find(query, MovieEntity.class);
    }

    public MovieDetailDTO movieDetail(String id){
        MovieEntity existed = movieRepository.findById(id).orElse(null);
        if(existed == null)
            throw Error.NotFoundError("Movie");

        MatchOperation match = Aggregation.match(Criteria.where("_id").in(existed.getCategories()));

        AggregationResults<CategoryEntity> result = mongoTemplate.aggregate(Aggregation.newAggregation(match), "mr_category", CategoryEntity.class);

        MovieDetailDTO detail = new MovieDetailDTO();
        BeanUtils.copyProperties(existed, detail);

        detail.setCategoriesObj(result.getMappedResults());

        return detail;
    }
}
