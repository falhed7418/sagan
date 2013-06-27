package org.springframework.site.blog.web;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.site.blog.*;
import org.springframework.ui.ExtendedModelMap;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.site.blog.PostCategory.ENGINEERING;

public class BlogControllerTests {

	private static final int TEST_PAGE = 1;
	public static final PostCategory TEST_CATEGORY = ENGINEERING;

	@Mock
    BlogService blogService;

	private BlogController controller;
	private ExtendedModelMap model = new ExtendedModelMap();
	private final List<Post> posts = new ArrayList<Post>();
	private final PaginationInfo paginationInfo = new PaginationInfo(new PageRequest(TEST_PAGE,10), 20);
	private final ResultList<Post> results = new ResultList<Post>(posts, paginationInfo);

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		controller = new BlogController(blogService);
		posts.add(PostBuilder.post().build());
		when(blogService.getPublishedPosts(any(Pageable.class))).thenReturn(results);
		when(blogService.getPublishedBroadcastPosts(any(Pageable.class))).thenReturn(results);
		when(blogService.getPublishedPosts(eq(TEST_CATEGORY), any(Pageable.class))).thenReturn(results);
	}

	private void assertThatPostsAreInModel() {
		assertThat((List<Post>) model.get("posts"), is(posts));
	}

	@Test
	public void anyListPosts_providesAllCategoriesInModel() {
		controller.listPosts(model, TEST_PAGE);
		assertThat((PostCategory[]) model.get("categories"), is(PostCategory.values()));
	}

	@Test
	public void anyListPosts_providesPaginationInfoInModel(){
		controller.listPosts(model, TEST_PAGE);
		assertThat((PaginationInfo) model.get("paginationInfo"), is(paginationInfo));
	}

	@Test(expected = BlogPostsNotFound.class)
	public void throwsExceptionWhenPostsNotFound() {
		ArrayList<Post> noPosts = new ArrayList<Post>();
		ResultList<Post> emptyResults = new ResultList<Post>(noPosts, mock(PaginationInfo.class));
		when(blogService.getPublishedPosts(any(Pageable.class))).thenReturn(emptyResults);
		controller.listPosts(model, TEST_PAGE);
	}

	@Test
	public void viewNameForAllPosts() {
		assertThat(controller.listPosts(model, TEST_PAGE), is("blog/index"));
	}

	@Test
	public void viewNameForBroadcastPosts() throws Exception {
		assertThat(controller.listBroadcasts(model, TEST_PAGE), is("blog/index"));
	}

	@Test
	public void viewNameForSinglePost() {
		assertThat(controller.showPost(1L, "not important", model), is("blog/show"));
	}

	@Test
	public void viewNameForCategoryPosts() {
		String view = controller.listPostsForCategory(TEST_CATEGORY, model, TEST_PAGE);
		assertThat(view, is("blog/index"));
	}

	@Test
	public void singlePostInModelForOnePost() {
		Post post = PostBuilder.post().build();
		when(blogService.getPublishedPost(post.getId())).thenReturn(post);
		controller.showPost(post.getId(), "1-post-title", model);
		assertThat((Post) model.get("post"), is(post));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void postsInModelForAllPosts(){
		controller.listPosts(model, TEST_PAGE);
		assertThatPostsAreInModel();
	}

	@Test
	public void postsInModelForBroadcastPosts() throws Exception {
		controller.listBroadcasts(model, TEST_PAGE);
		assertThatPostsAreInModel();
	}

	@Test
	public void postsInModelForCategoryPosts() {
		controller.listPostsForCategory(TEST_CATEGORY, model, TEST_PAGE);
		assertThatPostsAreInModel();
	}

	@Test
	public void postsInModelForAllPostsAtomFeed(){
		controller.atomFeed(model);
		assertThatPostsAreInModel();
	}

	@Test
	public void listPosts_offsetsThePageToBeZeroIndexed(){
		controller.listPosts(model, TEST_PAGE);
		int zeroIndexedPage = TEST_PAGE - 1;
		BlogPostsPageRequest pageRequest = new BlogPostsPageRequest(zeroIndexedPage);
		verify(blogService).getPublishedPosts(eq(pageRequest));
	}
}
