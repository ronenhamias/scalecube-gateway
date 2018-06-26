package io.scalecube.gateway.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class GatewayMessageCodecTest {

  private final GatewayMessageCodec codec = new GatewayMessageCodec();
  private final ObjectMapper objectMapper = objectMapper();

  @Test
  public void testDecodeNoData() {
    ByteBuf input = toByteBuf(TestInputs.NO_DATA);

    GatewayMessage result = codec.decode(input);

    Assert.assertEquals(TestInputs.Q, result.qualifier());
    Assert.assertEquals(TestInputs.SID, result.streamId());
    Assert.assertEquals(TestInputs.SIG, result.signal());
    Assert.assertEquals(TestInputs.I, result.inactivity());
  }

  @Test
  public void testDecodeNullData() {
    Object nullData = "null";
    String stringData =
        String.format(TestInputs.STRING_DATA_PATTERN_Q_SIG_SID_D, TestInputs.Q, TestInputs.SIG, TestInputs.SID,
            nullData);

    ByteBuf input = toByteBuf(stringData);
    System.out.println("Parsing JSON:" + stringData);

    GatewayMessage result = codec.decode(input);

    Assert.assertEquals(TestInputs.Q, result.qualifier());
    Assert.assertEquals(TestInputs.SIG, result.signal());
    Assert.assertEquals(TestInputs.SID, result.streamId());
    Assert.assertNull(result.data());
  }

  @Test
  public void testDecodeNumberData() {
    Integer expectedData = 123;
    String stringData =
        String.format(TestInputs.STRING_DATA_PATTERN_Q_SIG_SID_D, TestInputs.Q, TestInputs.SIG, TestInputs.SID,
            expectedData);

    ByteBuf input = toByteBuf(stringData);
    System.out.println("Parsing JSON:" + stringData);

    GatewayMessage result = codec.decode(input);

    Assert.assertEquals(TestInputs.Q, result.qualifier());
    Assert.assertEquals(TestInputs.SIG, result.signal());
    Assert.assertEquals(TestInputs.SID, result.streamId());
    Assert.assertTrue(result.data() instanceof ByteBuf);
    Assert.assertEquals(expectedData, Integer.valueOf(((ByteBuf) result.data()).toString(StandardCharsets.UTF_8)));
  }

  @Test
  public void testDecodeNumberDataFirst() {
    Integer expectedData = 123;
    String stringData =
        String.format(TestInputs.STRING_DATA_PATTERN_D_SIG_SID_Q, expectedData, TestInputs.SIG, TestInputs.SID,
            TestInputs.Q);

    ByteBuf input = toByteBuf(stringData);
    System.out.println("Parsing JSON:" + stringData);

    GatewayMessage result = codec.decode(input);

    Assert.assertEquals(TestInputs.Q, result.qualifier());
    Assert.assertEquals(TestInputs.SIG, result.signal());
    Assert.assertEquals(TestInputs.SID, result.streamId());
    Assert.assertTrue(result.data() instanceof ByteBuf);
    Assert.assertEquals(expectedData, Integer.valueOf(((ByteBuf) result.data()).toString(StandardCharsets.UTF_8)));
  }

  @Test
  public void testDecodeStringData() {
    String expectedData = "\"test\"";
    String stringData =
        String.format(TestInputs.STRING_DATA_PATTERN_Q_SIG_SID_D, TestInputs.Q, TestInputs.SIG, TestInputs.SID,
            expectedData);

    ByteBuf input = toByteBuf(stringData);
    System.out.println("Parsing JSON:" + stringData);

    GatewayMessage result = codec.decode(input);

    Assert.assertEquals(TestInputs.Q, result.qualifier());
    Assert.assertEquals(TestInputs.SIG, result.signal());
    Assert.assertEquals(TestInputs.SID, result.streamId());
    Assert.assertTrue(result.data() instanceof ByteBuf);
    Assert.assertEquals(expectedData, ((ByteBuf) result.data()).toString(StandardCharsets.UTF_8));
  }

  @Test
  public void testDecodeBooleanData() {
    Boolean expectedData = Boolean.FALSE;
    String stringData =
        String.format(TestInputs.STRING_DATA_PATTERN_Q_SIG_SID_D, TestInputs.Q, TestInputs.SIG, TestInputs.SID,
            expectedData);

    ByteBuf input = toByteBuf(stringData);
    System.out.println("Parsing JSON:" + stringData);

    GatewayMessage result = codec.decode(input);

    Assert.assertEquals(TestInputs.Q, result.qualifier());
    Assert.assertEquals(TestInputs.SIG, result.signal());
    Assert.assertEquals(TestInputs.SID, result.streamId());
    Assert.assertTrue(result.data() instanceof ByteBuf);
    Assert.assertEquals(expectedData, Boolean.valueOf(((ByteBuf) result.data()).toString(StandardCharsets.UTF_8)));
  }

  @Test
  public void testDecodePojoData() {
    String expectedData = "{\"text\":\"someValue\", \"id\":12345, \"empty\":null, \"embedded\":{\"id\":123}}";
    String stringData =
        String.format(TestInputs.STRING_DATA_PATTERN_Q_SIG_SID_D, TestInputs.Q, TestInputs.SIG, TestInputs.SID,
            expectedData);

    ByteBuf input = toByteBuf(stringData);
    System.out.println("Parsing JSON:" + stringData);

    GatewayMessage result = codec.decode(input);

    Assert.assertEquals(TestInputs.Q, result.qualifier());
    Assert.assertEquals(TestInputs.SIG, result.signal());
    Assert.assertEquals(TestInputs.SID, result.streamId());
    Assert.assertTrue(result.data() instanceof ByteBuf);
    Assert.assertEquals(expectedData, ((ByteBuf) result.data()).toString(StandardCharsets.UTF_8));
  }

  @Test
  public void testDecodeArrayData() {
    String expectedData = "[{\"id\":1}, {\"id\":2}, {\"id\":3}]";
    String stringData =
        String.format(TestInputs.STRING_DATA_PATTERN_Q_SIG_SID_D, TestInputs.Q, TestInputs.SIG, TestInputs.SID,
            expectedData);

    ByteBuf input = toByteBuf(stringData);
    System.out.println("Parsing JSON:" + stringData);

    GatewayMessage result = codec.decode(input);

    Assert.assertEquals(TestInputs.Q, result.qualifier());
    Assert.assertEquals(TestInputs.SIG, result.signal());
    Assert.assertEquals(TestInputs.SID, result.streamId());
    Assert.assertTrue(result.data() instanceof ByteBuf);
    Assert.assertEquals(expectedData, ((ByteBuf) result.data()).toString(StandardCharsets.UTF_8));
  }

  @Test
  public void testEncodePojoData() throws Exception {
    TestInputs.Entity data = new TestInputs.Entity("test", 123, true);
    GatewayMessage expected = GatewayMessage.builder()
        .qualifier(TestInputs.Q)
        .streamId(TestInputs.SID)
        .signal(TestInputs.SIG)
        .data(toByteBuf(data))
        .build();
    ByteBuf bb = codec.encode(expected);

    GatewayMessage actual = fromByteBuf(bb, TestInputs.Entity.class);

    Assert.assertEquals(expected.qualifier(), actual.qualifier());
    Assert.assertEquals(expected.signal(), actual.signal());
    Assert.assertEquals(expected.streamId(), actual.streamId());
    Assert.assertEquals(expected.inactivity(), actual.inactivity());
    Assert.assertEquals(data, actual.data());
  }

  @Test
  public void testEncodeNumberData() throws Exception {
    Integer data = -213;
    GatewayMessage expected = GatewayMessage.builder()
        .qualifier(TestInputs.Q)
        .streamId(TestInputs.SID)
        .signal(TestInputs.SIG)
        .data(toByteBuf(data))
        .build();
    ByteBuf bb = codec.encode(expected);
    GatewayMessage actual = fromByteBuf(bb, Integer.class);

    Assert.assertEquals(expected.qualifier(), actual.qualifier());
    Assert.assertEquals(expected.signal(), actual.signal());
    Assert.assertEquals(expected.streamId(), actual.streamId());
    Assert.assertEquals(expected.inactivity(), actual.inactivity());
    Assert.assertEquals(data, actual.data());
  }

  @Test
  public void testEncodeBooleanData() throws Exception {
    Boolean data = true;
    GatewayMessage expected = GatewayMessage.builder()
        .qualifier(TestInputs.Q)
        .streamId(TestInputs.SID)
        .signal(TestInputs.SIG)
        .data(toByteBuf(data))
        .build();
    ByteBuf bb = codec.encode(expected);

    GatewayMessage actual = fromByteBuf(bb, Boolean.class);

    Assert.assertEquals(expected.qualifier(), actual.qualifier());
    Assert.assertEquals(expected.signal(), actual.signal());
    Assert.assertEquals(expected.streamId(), actual.streamId());
    Assert.assertEquals(expected.inactivity(), actual.inactivity());
    Assert.assertEquals(data, actual.data());
  }

  private ByteBuf toByteBuf(String data) {
    ByteBuf bb = ByteBufAllocator.DEFAULT.buffer();
    bb.writeBytes(data.getBytes());
    return bb;
  }

  private ByteBuf toByteBuf(Object object) throws IOException {
    ByteBuf bb = ByteBufAllocator.DEFAULT.buffer();
    objectMapper.writeValue((OutputStream) new ByteBufOutputStream(bb), object);
    return bb;
  }

  private GatewayMessage fromByteBuf(ByteBuf bb, Class<?> dataClass) throws IOException {
    // noinspection unchecked
    Map<String, Object> map = objectMapper.readValue((InputStream) new ByteBufInputStream(bb.slice()), HashMap.class);
    return GatewayMessage.builder()
        .qualifier((String) map.get(GatewayMessage.QUALIFIER_FIELD))
        .streamId((Integer) map.get(GatewayMessage.STREAM_ID_FIELD))
        .signal((Integer) map.get(GatewayMessage.SIGNAL_FIELD))
        .inactivity((Integer) map.get(GatewayMessage.INACTIVITY_FIELD))
        .data(objectMapper.convertValue(map.get(GatewayMessage.DATA_FIELD), dataClass))
        .build();
  }

  private ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
    mapper.registerModule(new JavaTimeModule());
    return mapper;
  }
}
