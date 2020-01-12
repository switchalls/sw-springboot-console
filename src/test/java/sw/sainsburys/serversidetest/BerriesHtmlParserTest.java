package sw.sainsburys.serversidetest;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BerriesHtmlParserTest {

	private static final String EXPECTED_URL = "http://sw-test.com/berries";

	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();

	@Mock
	private JsoupConnectionProvider mockJsoupConnectionProvider;
	
	@Mock
	private Connection mockConnection;

	@InjectMocks
	private BerriesHtmlParser testSubject;

	@Before
	public void setUp() throws Exception {
		when(mockJsoupConnectionProvider.createConnectionFor(anyString()))
			.thenReturn(mockConnection);
	}

	@Test
	public void shouldThrowExceptionWhenCannotConnect() throws Exception {
		// Expects
		exceptionRule.expect(IOException.class);
		exceptionRule.expectMessage("Bang!");

		// Given
		when(mockConnection.get())
			.thenThrow(new IOException("Bang!"));
	
		// When
		testSubject.parse(EXPECTED_URL);
	}

	@Test
	public void shouldConnectToStatedUrl() throws Exception {
		// Given
		final Document expectedHtml = loadHtmlDocument("sainsburys-berries.html");

		when(mockConnection.get())
			.thenReturn(expectedHtml);

		// When
		final JSONObject result = testSubject.parse(EXPECTED_URL);

		// Then
		assertThat(result.get("title"), equalTo("Sainsbury's Strawberries 400g"));
		
		verify(mockJsoupConnectionProvider).createConnectionFor(eq(EXPECTED_URL));
	}

    private Document loadHtmlDocument(String path) throws Exception {
        final URL htmlUrl = this.getClass().getResource(path);
        assertThat("Cannot find file on classpath: " + path, htmlUrl, not(nullValue(URL.class)));

        try (InputStream fin = htmlUrl.openStream()) {
            return Jsoup.parse(fin, "UTF-8", "http://sw-test.com");
        }
    }

}