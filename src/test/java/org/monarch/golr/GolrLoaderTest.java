package org.monarch.golr;

import java.io.StringWriter;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.monarch.golr.beans.GolrCypherQuery;
import org.skyscreamer.jsonassert.JSONAssert;

import edu.sdsc.scigraph.internal.CypherUtil;
import edu.sdsc.scigraph.neo4j.DirectedRelationshipType;

public class GolrLoaderTest extends GolrLoadSetup {

  GolrLoader processor;
  StringWriter writer = new StringWriter();

  @Before
  public void setup() {
    EvidenceProcessorStub stub = new EvidenceProcessorStub(graphDb, new EvidenceAspectStub(), closureUtil, curieUtil);
    processor = new GolrLoader(graphDb, graph, new CypherUtil(graphDb), new ResultSerializerFactoryTestImpl(), stub);
  }

  @Test
  public void primitiveTypesSerialize() throws Exception {
    GolrCypherQuery query = new GolrCypherQuery("RETURN 'foo' as string, true as boolean, 1 as int, 1 as long, 1 as float, 1 as double");
    processor.process(query, writer);
    JSONAssert.assertEquals(getFixture("fixtures/primitives.json"), StringUtils.strip(writer.toString(), "[]"), false);
  }

  @Test
  public void defaultClosuresSerialize() throws Exception {
    GolrCypherQuery query = new GolrCypherQuery("MATCH (start)-[c:CAUSES]->(end) RETURN *");
    query.getProjection().put("start", "thing");
    query.getProjection().put("end", "otherThing");
    processor.process(query, writer);
    JSONAssert.assertEquals(getFixture("fixtures/simpleResult.json"), writer.toString(), true);
  }

  @Test
  public void relationshipClosureSerialization() throws Exception {
    GolrCypherQuery query = new GolrCypherQuery("MATCH (start)-[c:CAUSES]->(end) RETURN *");
    query.getProjection().put("c", "relationship");
    processor.process(query, writer);
    JSONAssert.assertEquals(getFixture("fixtures/relationshipResult.json"), writer.toString(), true);
  }

  @Test
  public void customClosuresSerialize() throws Exception {
    GolrCypherQuery query = new GolrCypherQuery("MATCH (start)-[c:CAUSES]->(end) RETURN *");
    query.getProjection().put("start", "thing");
    query.getProjection().put("end", "otherThing");
    query.getTypes().put("end", new DirectedRelationshipType("partOf", "OUTGOING"));
    processor.process(query, writer);
    JSONAssert.assertEquals(getFixture("fixtures/customClosureTypeResult.json"), writer.toString(), true);
  }

}