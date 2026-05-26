package com.connexta.ddf.catalog.direct;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.connexta.jsonrpc.RpcMethod;
import com.connexta.jsonrpc.impl.ErrorImpl;
import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeRegistry;
import ddf.catalog.data.AttributeType.AttributeFormat;
import ddf.catalog.data.InjectableAttribute;
import ddf.catalog.data.MetacardType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MetacardTypeMethodsTest {

  private static final String METACARD_TYPE_NAME = "testMetacardType";

  private static final String OTHER_METACARD_TYPE_NAME = "otherMetacardType";

  private static final String ATTRIBUTE_NAME = "testAttribute";

  private static final String INJECTABLE_ATTR_NAME = "injectableAttr";

  private MetacardTypeMethods metacardTypeMethods;

  @Mock private InjectableAttribute injectableAttribute;

  @Mock private AttributeRegistry attributeRegistry;

  @Mock private MetacardType metacardType;

  @Mock private MetacardType otherMetacardType;

  @Mock private AttributeDescriptor attributeDescriptor;

  @Mock private AttributeDescriptor injectableDescriptor;

  @Mock private ddf.catalog.data.AttributeType<?> attributeType;

  @Mock private ddf.catalog.data.AttributeType<?> injectableAttributeType;

  @BeforeEach
  public void setUp() {
    initMocks(this);

    when(metacardType.getName()).thenReturn(METACARD_TYPE_NAME);
    when(metacardType.getAttributeDescriptors())
        .thenReturn(Collections.singleton(attributeDescriptor));

    when(otherMetacardType.getName()).thenReturn(OTHER_METACARD_TYPE_NAME);
    when(otherMetacardType.getAttributeDescriptors()).thenReturn(Collections.emptySet());

    when(attributeDescriptor.getName()).thenReturn(ATTRIBUTE_NAME);
    doReturn(attributeType).when(attributeDescriptor).getType();
    when(attributeType.getAttributeFormat()).thenReturn(AttributeFormat.STRING);
    when(attributeDescriptor.isMultiValued()).thenReturn(false);
    when(attributeDescriptor.isIndexed()).thenReturn(true);
    when(attributeDescriptor.isStored()).thenReturn(true);
    when(attributeDescriptor.isTokenized()).thenReturn(false);

    when(injectableAttribute.attribute()).thenReturn(INJECTABLE_ATTR_NAME);
    when(injectableAttribute.metacardTypes()).thenReturn(Collections.emptySet());

    when(injectableDescriptor.getName()).thenReturn(INJECTABLE_ATTR_NAME);
    doReturn(injectableAttributeType).when(injectableDescriptor).getType();
    when(injectableAttributeType.getAttributeFormat()).thenReturn(AttributeFormat.STRING);
    when(injectableDescriptor.isMultiValued()).thenReturn(false);
    when(injectableDescriptor.isIndexed()).thenReturn(true);
    when(injectableDescriptor.isStored()).thenReturn(true);
    when(injectableDescriptor.isTokenized()).thenReturn(false);

    when(attributeRegistry.lookup(INJECTABLE_ATTR_NAME))
        .thenReturn(Optional.of(injectableDescriptor));
  }

  @Test
  public void testGetMethodsReturnsCorrectKeys() {
    List<MetacardType> metacardTypes = Collections.singletonList(metacardType);
    List<InjectableAttribute> injectableAttributes = Collections.singletonList(injectableAttribute);

    metacardTypeMethods =
        new MetacardTypeMethods(injectableAttributes, attributeRegistry, metacardTypes);

    Map<String, RpcMethod> methods = metacardTypeMethods.getMethods();

    assertThat(methods, notNullValue());
    assertThat(methods.containsKey("ddf.catalog/allMetacardTypes"), is(true));
    assertThat(methods.containsKey("ddf.catalog/metacardType"), is(true));
    assertThat(methods.keySet(), hasSize(2));
  }

  @Test
  public void testGetAllMetacardTypes() {
    List<MetacardType> metacardTypes = Collections.singletonList(metacardType);
    List<InjectableAttribute> injectableAttributes = Collections.emptyList();

    metacardTypeMethods =
        new MetacardTypeMethods(injectableAttributes, attributeRegistry, metacardTypes);

    Map<String, RpcMethod> methods = metacardTypeMethods.getMethods();
    RpcMethod allTypesMethod = methods.get("ddf.catalog/allMetacardTypes");

    Object result = allTypesMethod.apply(Collections.emptyMap());

    assertThat(result, instanceOf(Map.class));
    @SuppressWarnings("unchecked")
    Map<String, Object> resultMap = (Map<String, Object>) result;
    assertThat(resultMap.containsKey("metacardTypes"), is(true));
    @SuppressWarnings("unchecked")
    Map<String, Object> metacardTypesResult = (Map<String, Object>) resultMap.get("metacardTypes");
    assertThat(metacardTypesResult.containsKey(METACARD_TYPE_NAME), is(true));
  }

  @Test
  public void testGetAllMetacardTypesWithMultipleTypes() {
    List<MetacardType> metacardTypes = new ArrayList<>();
    metacardTypes.add(metacardType);
    metacardTypes.add(otherMetacardType);
    List<InjectableAttribute> injectableAttributes = Collections.emptyList();

    metacardTypeMethods =
        new MetacardTypeMethods(injectableAttributes, attributeRegistry, metacardTypes);

    Map<String, RpcMethod> methods = metacardTypeMethods.getMethods();
    RpcMethod allTypesMethod = methods.get("ddf.catalog/allMetacardTypes");

    Object result = allTypesMethod.apply(Collections.emptyMap());

    assertThat(result, instanceOf(Map.class));
    @SuppressWarnings("unchecked")
    Map<String, Object> resultMap = (Map<String, Object>) result;
    @SuppressWarnings("unchecked")
    Map<String, Object> metacardTypesResult = (Map<String, Object>) resultMap.get("metacardTypes");
    assertThat(metacardTypesResult.containsKey(METACARD_TYPE_NAME), is(true));
    assertThat(metacardTypesResult.containsKey(OTHER_METACARD_TYPE_NAME), is(true));
  }

  @Test
  public void testGetMetacardTypeSuccess() {
    List<MetacardType> metacardTypes = Collections.singletonList(metacardType);
    List<InjectableAttribute> injectableAttributes = Collections.emptyList();

    metacardTypeMethods =
        new MetacardTypeMethods(injectableAttributes, attributeRegistry, metacardTypes);

    Map<String, RpcMethod> methods = metacardTypeMethods.getMethods();
    RpcMethod getTypeMethod = methods.get("ddf.catalog/metacardType");

    Map<String, Object> params = new HashMap<>();
    params.put("metacardType", METACARD_TYPE_NAME);

    Object result = getTypeMethod.apply(params);

    assertThat(result, instanceOf(Map.class));
    @SuppressWarnings("unchecked")
    Map<String, Object> resultMap = (Map<String, Object>) result;
    assertThat(resultMap.containsKey("metacardTypes"), is(true));
  }

  @Test
  public void testGetMetacardTypeNotFound() {
    List<MetacardType> metacardTypes = Collections.singletonList(metacardType);
    List<InjectableAttribute> injectableAttributes = Collections.emptyList();

    metacardTypeMethods =
        new MetacardTypeMethods(injectableAttributes, attributeRegistry, metacardTypes);

    Map<String, RpcMethod> methods = metacardTypeMethods.getMethods();
    RpcMethod getTypeMethod = methods.get("ddf.catalog/metacardType");

    Map<String, Object> params = new HashMap<>();
    params.put("metacardType", "nonExistentType");

    Object result = getTypeMethod.apply(params);

    assertThat(result, instanceOf(ErrorImpl.class));
    ErrorImpl error = (ErrorImpl) result;
    assertThat(error.getMessage(), is("Could not find the requested metacardType"));
  }

  @Test
  public void testGetMetacardTypeInvalidParamsNotString() {
    List<MetacardType> metacardTypes = Collections.singletonList(metacardType);
    List<InjectableAttribute> injectableAttributes = Collections.emptyList();

    metacardTypeMethods =
        new MetacardTypeMethods(injectableAttributes, attributeRegistry, metacardTypes);

    Map<String, RpcMethod> methods = metacardTypeMethods.getMethods();
    RpcMethod getTypeMethod = methods.get("ddf.catalog/metacardType");

    Map<String, Object> params = new HashMap<>();
    params.put("metacardType", 12345);

    Object result = getTypeMethod.apply(params);

    assertThat(result, instanceOf(ErrorImpl.class));
    ErrorImpl error = (ErrorImpl) result;
    assertThat(error.getMessage(), is("`metacardType` must be present and a string value"));
  }

  @Test
  public void testGetMetacardTypeMissingParam() {
    List<MetacardType> metacardTypes = Collections.singletonList(metacardType);
    List<InjectableAttribute> injectableAttributes = Collections.emptyList();

    metacardTypeMethods =
        new MetacardTypeMethods(injectableAttributes, attributeRegistry, metacardTypes);

    Map<String, RpcMethod> methods = metacardTypeMethods.getMethods();
    RpcMethod getTypeMethod = methods.get("ddf.catalog/metacardType");

    Map<String, Object> params = new HashMap<>();

    Object result = getTypeMethod.apply(params);

    assertThat(result, instanceOf(ErrorImpl.class));
  }

  @Test
  public void testGetMetacardTypeMapIncludesAttributeDescriptors() {
    List<MetacardType> metacardTypes = Collections.singletonList(metacardType);
    List<InjectableAttribute> injectableAttributes = Collections.emptyList();

    metacardTypeMethods =
        new MetacardTypeMethods(injectableAttributes, attributeRegistry, metacardTypes);

    Object result = metacardTypeMethods.getMetacardTypeMap(metacardTypes);

    assertThat(result, instanceOf(Map.class));
    @SuppressWarnings("unchecked")
    Map<String, Object> resultMap = (Map<String, Object>) result;
    assertThat(resultMap.containsKey("metacardTypes"), is(true));
    @SuppressWarnings("unchecked")
    Map<String, Object> metacardTypesResult = (Map<String, Object>) resultMap.get("metacardTypes");
    assertThat(metacardTypesResult.containsKey(METACARD_TYPE_NAME), is(true));
    @SuppressWarnings("unchecked")
    Map<String, Object> attributes =
        (Map<String, Object>) metacardTypesResult.get(METACARD_TYPE_NAME);
    assertThat(attributes.containsKey(ATTRIBUTE_NAME), is(true));
    @SuppressWarnings("unchecked")
    Map<String, Object> attributeProperties = (Map<String, Object>) attributes.get(ATTRIBUTE_NAME);
    assertThat(attributeProperties.get("type"), is("STRING"));
    assertThat(attributeProperties.get("multivalued"), is(false));
    assertThat(attributeProperties.get("indexed"), is(true));
    assertThat(attributeProperties.get("stored"), is(true));
    assertThat(attributeProperties.get("tokenized"), is(false));
    assertThat(attributeProperties.get("id"), is(ATTRIBUTE_NAME));
    assertThat(attributeProperties.get("isInjected"), is(false));
  }

  @Test
  public void testInjectableAttributesAddedWhenNotInMetacardType() {
    List<MetacardType> metacardTypes = Collections.singletonList(otherMetacardType);
    List<InjectableAttribute> injectableAttributes = Collections.singletonList(injectableAttribute);

    metacardTypeMethods =
        new MetacardTypeMethods(injectableAttributes, attributeRegistry, metacardTypes);

    Object result = metacardTypeMethods.getMetacardTypeMap(metacardTypes);

    assertThat(result, instanceOf(Map.class));
    @SuppressWarnings("unchecked")
    Map<String, Object> resultMap = (Map<String, Object>) result;
    @SuppressWarnings("unchecked")
    Map<String, Object> metacardTypesResult = (Map<String, Object>) resultMap.get("metacardTypes");
    assertThat(metacardTypesResult.containsKey(OTHER_METACARD_TYPE_NAME), is(true));
    @SuppressWarnings("unchecked")
    Map<String, Object> attributes =
        (Map<String, Object>) metacardTypesResult.get(OTHER_METACARD_TYPE_NAME);
    assertThat(attributes.containsKey(INJECTABLE_ATTR_NAME), is(true));
    @SuppressWarnings("unchecked")
    Map<String, Object> attributeProperties =
        (Map<String, Object>) attributes.get(INJECTABLE_ATTR_NAME);
    assertThat(attributeProperties.get("isInjected"), is(true));
  }

  @Test
  public void testInjectableAttributesNotDuplicatedWhenAlreadyPresent() {
    List<MetacardType> metacardTypes = Collections.singletonList(metacardType);
    List<InjectableAttribute> injectableAttributes = Collections.singletonList(injectableAttribute);

    metacardTypeMethods =
        new MetacardTypeMethods(injectableAttributes, attributeRegistry, metacardTypes);

    Object result = metacardTypeMethods.getMetacardTypeMap(metacardTypes);

    assertThat(result, instanceOf(Map.class));
    @SuppressWarnings("unchecked")
    Map<String, Object> resultMap = (Map<String, Object>) result;
    @SuppressWarnings("unchecked")
    Map<String, Object> metacardTypesResult = (Map<String, Object>) resultMap.get("metacardTypes");
    @SuppressWarnings("unchecked")
    Map<String, Object> attributes =
        (Map<String, Object>) metacardTypesResult.get(METACARD_TYPE_NAME);
    assertThat(attributes.containsKey(ATTRIBUTE_NAME), is(true));
  }

  @Test
  public void testInjectableAttributesForSpecificMetacardTypes() {
    when(injectableAttribute.metacardTypes()).thenReturn(Collections.singleton(METACARD_TYPE_NAME));

    List<MetacardType> metacardTypes = new ArrayList<>();
    metacardTypes.add(metacardType);
    metacardTypes.add(otherMetacardType);
    List<InjectableAttribute> injectableAttributes = Collections.singletonList(injectableAttribute);

    metacardTypeMethods =
        new MetacardTypeMethods(injectableAttributes, attributeRegistry, metacardTypes);

    Object result = metacardTypeMethods.getMetacardTypeMap(metacardTypes);

    assertThat(result, instanceOf(Map.class));
    @SuppressWarnings("unchecked")
    Map<String, Object> resultMap = (Map<String, Object>) result;
    @SuppressWarnings("unchecked")
    Map<String, Object> metacardTypesResult = (Map<String, Object>) resultMap.get("metacardTypes");

    // METACARD_TYPE_NAME should have the injectable attribute
    @SuppressWarnings("unchecked")
    Map<String, Object> metacard1Attrs =
        (Map<String, Object>) metacardTypesResult.get(METACARD_TYPE_NAME);
    assertThat(metacard1Attrs.containsKey(INJECTABLE_ATTR_NAME), is(true));

    // OTHER_METACARD_TYPE_NAME should NOT have the injectable attribute
    @SuppressWarnings("unchecked")
    Map<String, Object> metacard2Attrs =
        (Map<String, Object>) metacardTypesResult.get(OTHER_METACARD_TYPE_NAME);
    assertThat(metacard2Attrs.containsKey(INJECTABLE_ATTR_NAME), is(false));
  }

  @Test
  public void testInjectableAttributeNotAddedWhenNotFoundInRegistry() {
    when(attributeRegistry.lookup(INJECTABLE_ATTR_NAME)).thenReturn(Optional.empty());

    List<MetacardType> metacardTypes = Collections.singletonList(otherMetacardType);
    List<InjectableAttribute> injectableAttributes = Collections.singletonList(injectableAttribute);

    metacardTypeMethods =
        new MetacardTypeMethods(injectableAttributes, attributeRegistry, metacardTypes);

    Object result = metacardTypeMethods.getMetacardTypeMap(metacardTypes);

    assertThat(result, instanceOf(Map.class));
    @SuppressWarnings("unchecked")
    Map<String, Object> resultMap = (Map<String, Object>) result;
    @SuppressWarnings("unchecked")
    Map<String, Object> metacardTypesResult = (Map<String, Object>) resultMap.get("metacardTypes");
    @SuppressWarnings("unchecked")
    Map<String, Object> attributes =
        (Map<String, Object>) metacardTypesResult.get(OTHER_METACARD_TYPE_NAME);
    assertThat(attributes.containsKey(INJECTABLE_ATTR_NAME), is(false));
  }

  @Test
  public void testInjectableAttributesMergedIntoExistingMetacardType() {
    when(otherMetacardType.getAttributeDescriptors())
        .thenReturn(Collections.singleton(attributeDescriptor));

    List<MetacardType> metacardTypes = new ArrayList<>();
    metacardTypes.add(metacardType);
    metacardTypes.add(otherMetacardType);
    List<InjectableAttribute> injectableAttributes = Collections.singletonList(injectableAttribute);

    metacardTypeMethods =
        new MetacardTypeMethods(injectableAttributes, attributeRegistry, metacardTypes);

    Object result = metacardTypeMethods.getMetacardTypeMap(metacardTypes);

    assertThat(result, instanceOf(Map.class));
    @SuppressWarnings("unchecked")
    Map<String, Object> resultMap = (Map<String, Object>) result;
    @SuppressWarnings("unchecked")
    Map<String, Object> metacardTypesResult = (Map<String, Object>) resultMap.get("metacardTypes");

    // otherMetacardType should have both its own attribute and the injectable one
    @SuppressWarnings("unchecked")
    Map<String, Object> otherMetacardAttrs =
        (Map<String, Object>) metacardTypesResult.get(OTHER_METACARD_TYPE_NAME);
    assertThat(otherMetacardAttrs.containsKey(ATTRIBUTE_NAME), is(true));
    assertThat(otherMetacardAttrs.containsKey(INJECTABLE_ATTR_NAME), is(true));
  }

  @Test
  public void testGetMetacardTypeMapWithEmptyList() {
    List<MetacardType> metacardTypes = Collections.emptyList();
    List<InjectableAttribute> injectableAttributes = Collections.emptyList();

    metacardTypeMethods =
        new MetacardTypeMethods(injectableAttributes, attributeRegistry, metacardTypes);

    Object result = metacardTypeMethods.getMetacardTypeMap(metacardTypes);

    assertThat(result, instanceOf(Map.class));
    @SuppressWarnings("unchecked")
    Map<String, Object> resultMap = (Map<String, Object>) result;
    assertThat(resultMap.containsKey("metacardTypes"), is(true));
    @SuppressWarnings("unchecked")
    Map<String, Object> metacardTypesResult = (Map<String, Object>) resultMap.get("metacardTypes");
    assertThat(metacardTypesResult.isEmpty(), is(true));
  }

  @Test
  public void testMultiValuedAttributeDescriptor() {
    when(attributeDescriptor.isMultiValued()).thenReturn(true);

    List<MetacardType> metacardTypes = Collections.singletonList(metacardType);
    List<InjectableAttribute> injectableAttributes = Collections.emptyList();

    metacardTypeMethods =
        new MetacardTypeMethods(injectableAttributes, attributeRegistry, metacardTypes);

    Object result = metacardTypeMethods.getMetacardTypeMap(metacardTypes);

    assertThat(result, instanceOf(Map.class));
    @SuppressWarnings("unchecked")
    Map<String, Object> resultMap = (Map<String, Object>) result;
    @SuppressWarnings("unchecked")
    Map<String, Object> metacardTypesResult = (Map<String, Object>) resultMap.get("metacardTypes");
    @SuppressWarnings("unchecked")
    Map<String, Object> attributes =
        (Map<String, Object>) metacardTypesResult.get(METACARD_TYPE_NAME);
    @SuppressWarnings("unchecked")
    Map<String, Object> attributeProperties = (Map<String, Object>) attributes.get(ATTRIBUTE_NAME);
    assertThat(attributeProperties.get("multivalued"), is(true));
  }

  @Test
  public void testTokenizedAttributeDescriptor() {
    when(attributeDescriptor.isTokenized()).thenReturn(true);

    List<MetacardType> metacardTypes = Collections.singletonList(metacardType);
    List<InjectableAttribute> injectableAttributes = Collections.emptyList();

    metacardTypeMethods =
        new MetacardTypeMethods(injectableAttributes, attributeRegistry, metacardTypes);

    Object result = metacardTypeMethods.getMetacardTypeMap(metacardTypes);

    assertThat(result, instanceOf(Map.class));
    @SuppressWarnings("unchecked")
    Map<String, Object> resultMap = (Map<String, Object>) result;
    @SuppressWarnings("unchecked")
    Map<String, Object> metacardTypesResult = (Map<String, Object>) resultMap.get("metacardTypes");
    @SuppressWarnings("unchecked")
    Map<String, Object> attributes =
        (Map<String, Object>) metacardTypesResult.get(METACARD_TYPE_NAME);
    @SuppressWarnings("unchecked")
    Map<String, Object> attributeProperties = (Map<String, Object>) attributes.get(ATTRIBUTE_NAME);
    assertThat(attributeProperties.get("tokenized"), is(true));
  }
}
