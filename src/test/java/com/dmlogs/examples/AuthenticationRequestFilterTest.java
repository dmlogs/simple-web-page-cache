package com.dmlogs.examples;

import static org.easymock.EasyMock.*;

import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.glassfish.jersey.internal.util.Base64;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.io.IOException;

@RunWith(EasyMockRunner.class)
public class AuthenticationRequestFilterTest extends EasyMockSupport {

    private AuthenticationRequestFilter filter;

    @Mock
    private ContainerRequestContext mockContext;
    @Mock
    private UriInfo mockUriInfo;

    private String validBase64UserAndPassword = Base64.encodeAsString("user:password");
    private String validBase64Password = Base64.encodeAsString("notuser:password");
    private String validBase64User = Base64.encodeAsString("user:notpassword");

    @Before
    public void initialize() {
        filter = new AuthenticationRequestFilter();

        expect(mockContext.getUriInfo())
                .andReturn(mockUriInfo);

    }

    @Test
    public void testPathApplicationWadl() throws IOException {
        setMockPath("application.wadl");

        replayAll();
        filter.filter(mockContext);
        verifyAll();
    }


    private void defaultMockPath() {
        setMockPath("default");
    }

    private void setMockPath(String path) {
        expect(mockUriInfo.getPath())
                .andReturn(path);
    }

    @Test
    public void testNoAuthorizationHeader() throws IOException {
        defaultMockPath();

        expect(mockContext.getHeaderString("Authorization"))
                .andReturn(null);

        mockContext.abortWith(isA(Response.class));
        expectLastCall().times(1);

        replayAll();
        filter.filter(mockContext);
        verifyAll();
    }

    @Test
    public void testMalformedAuthorizationNoDelimiter() throws IOException {
        defaultMockPath();

        expect(mockContext.getHeaderString("Authorization"))
                .andReturn("BasicNoDelimiter");

        mockContext.abortWith(isA(Response.class));
        expectLastCall().times(1);

        replayAll();
        filter.filter(mockContext);
        verifyAll();
    }



    @Test
    public void testMalformedAuthorizationMissingBasic() throws IOException {
        defaultMockPath();

        expect(mockContext.getHeaderString("Authorization"))
                .andReturn(String.format("NotBasic %s",validBase64UserAndPassword));

        mockContext.abortWith(isA(Response.class));
        expectLastCall().times(1);

        replayAll();
        filter.filter(mockContext);
        verifyAll();
    }

    @Test
    public void testMalformedAuthroizationNotBase64() throws IOException {
        defaultMockPath();

        expect(mockContext.getHeaderString("Authorization"))
                .andReturn("Basic NotBase64");

        mockContext.abortWith(isA(Response.class));
        expectLastCall().times(1);

        replayAll();
        filter.filter(mockContext);
        verifyAll();
    }

    @Test
    public void testAuthenticationIncorrectUser() throws IOException {
        defaultMockPath();

        expect(mockContext.getHeaderString("Authorization"))
                .andReturn(String.format("Basic %s",validBase64Password));

        mockContext.abortWith(isA(Response.class));
        expectLastCall().times(1);

        replayAll();
        filter.filter(mockContext);
        verifyAll();
    }

    @Test
    public void testAuthenticationIncorrectPassword() throws IOException {
        defaultMockPath();

        expect(mockContext.getHeaderString("Authorization"))
                .andReturn(String.format("Basic %s",validBase64User));

        mockContext.abortWith(isA(Response.class));
        expectLastCall().times(1);

        replayAll();
        filter.filter(mockContext);
        verifyAll();
    }

    @Test
    public void testAuthenticationSuccessful() throws IOException {
        defaultMockPath();

        expect(mockContext.getHeaderString("Authorization"))
                .andReturn(String.format("Basic %s",validBase64UserAndPassword));

        replayAll();
        filter.filter(mockContext);
        verifyAll();
    }

}
