package com.example.buildnest_ecommerce.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GenericMapperUtilTest {

    public static class Source {
        public String name;
        public int age;
        public String ignore;

        public Source() {
        }

        public Source(String name, int age, String ignore) {
            this.name = name;
            this.age = age;
            this.ignore = ignore;
        }
    }

    public static class Target {
        public String name;
        public Integer age;
        public String ignore;
    }

    public static class ParentSource {
        public String inherited;
    }

    public static class ChildSource extends ParentSource {
        public String child;
    }

    public static class ParentTarget {
        public String inherited;
    }

    public static class ChildTarget extends ParentTarget {
        public String child;
    }

    public static class PrimitiveSource {
        public boolean active;
        public int count;
    }

    public static class WrapperTarget {
        public Boolean active;
        public Integer count;
    }

    public static class NoDefaultCtor {
        public String value;

        public NoDefaultCtor(String value) {
            this.value = value;
        }
    }

    @Test
    void mapAndMapListCopyFields() {
        GenericMapperUtil util = new GenericMapperUtil();

        Source source = new Source("Alice", 30, "skip");
        Target target = util.map(source, Target.class);

        assertNotNull(target);
        assertEquals("Alice", target.name);
        assertEquals(30, target.age);

        List<Target> list = util.mapList(Arrays.asList(source), Target.class);
        assertEquals(1, list.size());
    }

    @Test
    void mergeUpdateAndShallowCopy() {
        GenericMapperUtil util = new GenericMapperUtil();

        Source target = new Source();
        Source source = new Source("Bob", 25, null);
        util.mergeUpdate(source, target);

        assertEquals("Bob", target.name);
        assertEquals(25, target.age);

        Source copy = util.shallowCopy(target);
        assertNotNull(copy);
        assertEquals(target.name, copy.name);
        assertEquals(target.age, copy.age);
    }

    @Test
    void toMapAndBuilder() {
        GenericMapperUtil util = new GenericMapperUtil();

        Source source = new Source("Charlie", 40, "ignored");
        Map<String, Object> map = util.toMap(source);
        assertTrue(map.containsKey("name"));
        assertTrue(map.containsKey("age"));

        Target target = new Target();
        GenericMapperUtil.MappingBuilder<Source, Target> builder = new GenericMapperUtil.MappingBuilder<>(source,
                target)
                .excludeField("ignore")
                .mapFieldWithTransform("name", "name", value -> value + "!");

        Target built = builder.build();
        assertEquals("Charlie!", built.name);
        assertEquals(40, built.age);
    }

    @Test
    void mapNullsAndMapListNulls() {
        GenericMapperUtil util = new GenericMapperUtil();

        assertNull(util.map(null, Target.class));
        assertTrue(util.mapList(null, Target.class).isEmpty());
    }

    @Test
    void mapCopiesInheritedFieldsAndPrimitiveWrappers() {
        GenericMapperUtil util = new GenericMapperUtil();

        ChildSource source = new ChildSource();
        source.inherited = "base";
        source.child = "child";

        ChildTarget target = util.map(source, ChildTarget.class);
        assertNotNull(target);
        assertEquals("base", target.inherited);
        assertEquals("child", target.child);

        PrimitiveSource primitiveSource = new PrimitiveSource();
        primitiveSource.active = true;
        primitiveSource.count = 5;

        WrapperTarget wrapperTarget = util.map(primitiveSource, WrapperTarget.class);
        assertEquals(Boolean.TRUE, wrapperTarget.active);
        assertEquals(5, wrapperTarget.count);
    }

    @Test
    void mapThrowsWhenNoDefaultConstructor() {
        GenericMapperUtil util = new GenericMapperUtil();

        assertThrows(RuntimeException.class, () -> util.map(new Source("A", 1, null), NoDefaultCtor.class));
    }

    @Test
    void mergeUpdateAndShallowCopyNullSafe() {
        GenericMapperUtil util = new GenericMapperUtil();

        assertDoesNotThrow(() -> util.mergeUpdate(null, new Source()));
        assertDoesNotThrow(() -> util.mergeUpdate(new Source("X", 1, null), null));
        assertNull(util.shallowCopy(null));
        assertTrue(util.toMap(null).isEmpty());
    }

    @Test
    void mappingBuilderHandlesExclusionsAndMissingFields() {
        Source source = new Source("Dora", 20, "exclude");
        Target target = new Target();

        GenericMapperUtil.MappingBuilder<Source, Target> builder = new GenericMapperUtil.MappingBuilder<>(source,
                target)
                .excludeField("ignore")
                .mapField("name", "name")
                .mapFieldWithTransform("name", "ignore", value -> null)
                .mapField("missing", "name");

        Target result = builder.build();
        assertEquals("Dora", result.name);
        assertNull(result.ignore);
    }
}
