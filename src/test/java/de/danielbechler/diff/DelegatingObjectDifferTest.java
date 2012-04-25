/*
 * Copyright 2012 Daniel Bechler
 *
 * This file is part of java-object-diff.
 *
 * java-object-diff is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * java-object-diff is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with java-object-diff.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.danielbechler.diff;

import de.danielbechler.diff.accessor.*;
import de.danielbechler.diff.mock.*;
import de.danielbechler.diff.node.*;
import de.danielbechler.diff.path.*;
import org.hamcrest.core.*;
import org.junit.*;
import org.mockito.*;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** @author Daniel Bechler */
public class DelegatingObjectDifferTest
{
	@Mock
	private Differ beanDiffer;
	@Mock
	private Differ mapDiffer;
	@Mock
	private Differ collectionDiffer;
	@Mock
	private Accessor accessor;
	@Mock
	private Node node;

	private DelegatingObjectDifferImpl differ;

	@Before
	public void setUp() throws Exception
	{
		MockitoAnnotations.initMocks(this);
		differ = new DelegatingObjectDifferImpl(beanDiffer, mapDiffer, collectionDiffer);
	}

	@Test
	public void testConstructionWithDefaultConstructor() throws Exception
	{
		differ = new DelegatingObjectDifferImpl();
		assertThat(differ.getBeanDiffer(), IsNull.notNullValue());
		assertThat(differ.getMapDiffer(), IsNull.notNullValue());
		assertThat(differ.getCollectionDiffer(), IsNull.notNullValue());
	}

	@Test
	public void testCompareWithCollection() throws Exception
	{
		differ.delegate(Node.ROOT, Instances.of(new RootAccessor(), new LinkedList<String>(), new LinkedList<String>()));
		Mockito.verify(collectionDiffer, Mockito.times(1)).compare(any(Node.class), any(Instances.class));
		Mockito.verify(mapDiffer, Mockito.never()).compare(any(Node.class), any(Instances.class));
		Mockito.verify(beanDiffer, Mockito.never()).compare(any(Node.class), any(Instances.class));
	}

	@Test
	public void testCompareWithMap() throws Exception
	{
		differ.delegate(Node.ROOT, Instances.of(new TreeMap<String, String>(), new TreeMap<String, String>()));
		Mockito.verify(collectionDiffer, Mockito.never()).compare(any(Node.class), any(Instances.class));
		Mockito.verify(mapDiffer, Mockito.times(1)).compare(any(Node.class), any(Instances.class));
		Mockito.verify(beanDiffer, Mockito.never()).compare(any(Node.class), any(Instances.class));
	}

	@Test
	public void testCompareWithSimpleType() throws Exception
	{
		differ.delegate(Node.ROOT, Instances.of("", ""));
		Mockito.verify(collectionDiffer, Mockito.never()).compare(any(Node.class), any(Instances.class));
		Mockito.verify(mapDiffer, Mockito.never()).compare(any(Node.class), any(Instances.class));
		Mockito.verify(beanDiffer, Mockito.times(1)).compare(any(Node.class), any(Instances.class));
	}

	@Test
	public void testCompareWithBean() throws Exception
	{
		differ.compare(new ObjectWithString(), new ObjectWithString());
		Mockito.verify(collectionDiffer, Mockito.never()).compare(any(Node.class), any(Instances.class));
		Mockito.verify(mapDiffer, Mockito.never()).compare(any(Node.class), any(Instances.class));
		Mockito.verify(beanDiffer, Mockito.times(1)).compare(any(Node.class), any(Instances.class));
	}

	@Test
	public void testGetConfiguration() throws Exception
	{
		assertThat(differ.getConfiguration(), IsNull.notNullValue());
	}

	@Test
	public void testSetConfiguration() throws Exception
	{
		final Configuration configuration = new Configuration();
		differ.setConfiguration(configuration);
		assertThat(differ.getConfiguration(), IsSame.sameInstance(configuration));
	}

	@Test
	public void testCompareWithIgnoredMapProperty()
	{
		final ObjectWithIgnoredMap working = new ObjectWithIgnoredMap();
		working.getMap().put("foo", "bar");
		final ObjectWithIgnoredMap base = new ObjectWithIgnoredMap();
		final ObjectDiffer objectDiffer = new DelegatingObjectDifferImpl();
		final Node node = objectDiffer.compare(working, base);
		Assert.assertThat(node.hasChanges(), Is.is(false));
		Assert.assertThat(node.hasChildren(), Is.is(false));
	}

	@Test
	public void testCompareWithIgnoredCollectionProperty()
	{
		final ObjectWithCollection working = new ObjectWithCollection();
		working.getCollection().add("foo");
		final ObjectWithCollection base = new ObjectWithCollection();
		final ObjectDiffer objectDiffer = new DelegatingObjectDifferImpl();
		objectDiffer.getConfiguration().withoutProperty(PropertyPath.with("collection"));
		final Node node = objectDiffer.compare(working, base);
		Assert.assertThat(node.hasChanges(), Is.is(false));
		Assert.assertThat(node.hasChildren(), Is.is(false));
	}
}