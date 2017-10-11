package io.greennav.routing;

/* disabled, fails in docker
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RoutingApplication.class)
@WebAppConfiguration
public class RoutingRestControllerTest {
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setupTestEnvironment() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testShortestPathFromThreeNodes() throws Exception {
        String[] algorithms = {"dijkstra", "astar"};
        for (String algorithm : algorithms) {
            String requestString = String.format("/route?from=4058432473&to=25195716&algorithm=%s", algorithm);
            mockMvc.perform(get(requestString))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$[0].id", is(4058432473L)))
                   .andExpect(jsonPath("$[1].id", is(4058432482L)))
                   .andExpect(jsonPath("$[2].id", is(25195716)));
        }
    }
}
*/
