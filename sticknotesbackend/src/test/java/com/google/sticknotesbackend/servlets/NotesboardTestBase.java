package com.google.sticknotesbackend.servlets;

import com.google.cloud.datastore.testing.LocalDatastoreHelper;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.cloud.NoCredentials;
import com.google.cloud.ServiceOptions;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.sticknotesbackend.models.Note;
import com.google.sticknotesbackend.models.User;
import com.google.sticknotesbackend.models.UserBoardRole;
import com.google.sticknotesbackend.models.Whiteboard;
import com.google.sticknotesbackend.serializers.UserBoardRoleSerializer;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.BeforeClass;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class NotesboardTestBase {
  protected final int OK = 200;
  protected final int CREATED = 201;
  protected final int BAD_REQUEST = 400;
  protected final int NO_CONTENT = 204;
  // Set up a helper so that the ApiProxy returns a valid environment for local testing.
  protected final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalMemcacheServiceTestConfig(), new LocalDatastoreServiceTestConfig());

  protected final LocalDatastoreHelper datastoreHelper = LocalDatastoreHelper.create(8484);

  protected Closeable session;

  @Mock
  protected HttpServletRequest mockRequest;
  @Mock
  protected HttpServletResponse mockResponse;
  protected StringWriter responseWriter;

  @BeforeClass
  public static void initializeObjectify() {
    // necessary setup to make Obejctify work
    DatastoreOptions options = DatastoreOptions.newBuilder().setProjectId("dummy").setHost("localhost:8484")
        .setCredentials(NoCredentials.getInstance()).setRetrySettings(ServiceOptions.getNoRetrySettings()).build();
    Datastore datastore = options.getService();
    ObjectifyService.init(new ObjectifyFactory(datastore));
    ObjectifyService.register(Whiteboard.class);
    ObjectifyService.register(User.class);
    ObjectifyService.register(UserBoardRole.class);
  }

  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    helper.setUp();
    session = ObjectifyService.begin();
  }

  @After
  public void tearDown() {
    session.close();
    helper.tearDown();
  }

  // helper method that constructs a testing object of Whiteboard
  protected Whiteboard getMockBoard() {
    Whiteboard board = new Whiteboard("test");
    board.creationDate = System.currentTimeMillis();
    // create dummy user and set this user as a creator of the board
    board.setCreator(new User("randomid", "googler@google.com", "nickname"));
    board.rows = 4;
    board.cols = 6;
    return board;
  }

  public Gson getBoardGsonParser() {
    GsonBuilder gson = new GsonBuilder();
    gson.registerTypeAdapter(UserBoardRole.class, new UserBoardRoleSerializer());
    Gson parser = gson.create();
    return parser;

  // helper method to create a note
  protected Note getMockNote() {
    return new Note(new User("randomuser", "googler", "googler@google.com"), "content", "color", 1, 2);
  }
}
