package io.greennav.routing;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

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
            mockMvc.perform(get("/4058432473/25195716/" + algorithm))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$[0].id", is(4058432473L)))
                   .andExpect(jsonPath("$[1].id", is(4058432482L)))
                   .andExpect(jsonPath("$[2].id", is(25195716)));
        }
    }
}
