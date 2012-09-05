/*******************************************************************************
 * * Copyright 2012 Impetus Infotech.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 ******************************************************************************/
package com.impetus.kundera.tests.crossdatastore.useraddress.datatype;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.cassandra.thrift.CfDef;
import org.apache.cassandra.thrift.ColumnDef;
import org.apache.cassandra.thrift.IndexType;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.KsDef;
import org.apache.cassandra.thrift.NotFoundException;
import org.apache.cassandra.thrift.SchemaDisagreementException;
import org.apache.cassandra.thrift.TimedOutException;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.impetus.kundera.tests.cli.CassandraCli;
import com.impetus.kundera.tests.crossdatastore.useraddress.TwinAssociation;
import com.impetus.kundera.tests.crossdatastore.useraddress.datatype.entities.HabitatBiMTo1Char;
import com.impetus.kundera.tests.crossdatastore.useraddress.datatype.entities.PersonnelBiMTo1Int;

public class MTOBiAssociationIntTest extends TwinAssociation
{

    /**
     * Inits the.
     */
    @BeforeClass
    public static void init() throws Exception
    {
        List<Class> clazzz = new ArrayList<Class>(2);
        clazzz.add(PersonnelBiMTo1Int.class);
        clazzz.add(HabitatBiMTo1Char.class);
        init(clazzz, ALL_PUs_UNDER_TEST);
    }

    /**
     * Sets the up.
     * 
     * @throws Exception
     *             the exception
     */
    @Before
    public void setUp() throws Exception
    {
        setUpInternal();
    }

    @Test
    public void testCRUD()
    {
        tryOperation();
    }

    @Override
    protected void insert()
    {
        PersonnelBiMTo1Int person1 = new PersonnelBiMTo1Int();
        person1.setPersonId(12345);
        person1.setPersonName("Amresh");

        PersonnelBiMTo1Int person2 = new PersonnelBiMTo1Int();
        person2.setPersonId(12346);
        person2.setPersonName("Vivek");

        HabitatBiMTo1Char address = new HabitatBiMTo1Char();
        address.setAddressId('A');
        address.setStreet("AAAAAAAAAAAAA");

        person1.setAddress(address);
        person2.setAddress(address);

        Set<PersonnelBiMTo1Int> persons = new HashSet<PersonnelBiMTo1Int>();
        persons.add(person1);
        persons.add(person2);

        dao.savePersons(persons);
        col.add(person1);
        col.add(person2);
        col.add(address);

    }   
    
    @Override
    protected void find()
    {
        // Find Person 1
        PersonnelBiMTo1Int p1 = (PersonnelBiMTo1Int) dao.findPerson(PersonnelBiMTo1Int.class, 12345);
        assertPerson1(p1);

        // Find Person 2
        PersonnelBiMTo1Int p2 = (PersonnelBiMTo1Int) dao.findPerson(PersonnelBiMTo1Int.class, 12346);
        assertPerson2(p2);

    }

    
    

    @Override
    protected void findPersonByIdColumn()
    {
        PersonnelBiMTo1Int p = (PersonnelBiMTo1Int) dao.findPersonByIdColumn(PersonnelBiMTo1Int.class, 12345);    
        assertPerson1(p);
    }

    @Override
    protected void findPersonByName()
    {
        List<PersonnelBiMTo1Int> persons = dao.findPersonByName(PersonnelBiMTo1Int.class, "Amresh");
        Assert.assertNotNull(persons);
        Assert.assertFalse(persons.isEmpty());
        Assert.assertTrue(persons.size() == 1);
        PersonnelBiMTo1Int person = persons.get(0);
        assertPerson1(person);
    }

    @Override
    protected void findAddressByIdColumn()
    {
    }

    @Override
    protected void findAddressByStreet()
    {
    }    

    @Override
    protected void update()
    {
    }

    @Override
    protected void remove()
    {
    }
    
    /**
     * @param p2
     */
    private void assertPerson2(PersonnelBiMTo1Int p2)
    {
        Assert.assertNotNull(p2);
        Assert.assertEquals(12346, p2.getPersonId());
        Assert.assertEquals("Vivek", p2.getPersonName());

        HabitatBiMTo1Char add2 = p2.getAddress();
        Assert.assertNotNull(add2);

        Assert.assertEquals('A', add2.getAddressId());
        Set<PersonnelBiMTo1Int> people2 = add2.getPeople();
        Assert.assertNotNull(people2);
        Assert.assertFalse(people2.isEmpty());
        Assert.assertEquals(2, people2.size());
    }

    /**
     * @param p1
     */
    private void assertPerson1(PersonnelBiMTo1Int p1)
    {
        Assert.assertNotNull(p1);
        Assert.assertEquals(12345, p1.getPersonId());
        Assert.assertEquals("Amresh", p1.getPersonName());

        HabitatBiMTo1Char add = p1.getAddress();
        Assert.assertNotNull(add);

        Assert.assertEquals('A', add.getAddressId());
        Set<PersonnelBiMTo1Int> people = add.getPeople();
        Assert.assertNotNull(people);
        Assert.assertFalse(people.isEmpty());
        Assert.assertEquals(2, people.size());
    }

    /**
     * Tear down.
     * 
     * @throws Exception
     *             the exception
     */
    @After
    public void tearDown() throws Exception
    {
        tearDownInternal();
    }

    @Override
    protected void loadDataForPERSONNEL() throws TException, InvalidRequestException, UnavailableException,
            TimedOutException, SchemaDisagreementException
    {
        KsDef ksDef = null;

        CfDef cfDef = new CfDef();
        cfDef.name = "PERSONNEL";
        cfDef.keyspace = "KunderaTests";
        // cfDef.column_type = "Super";
        cfDef.setComparator_type("UTF8Type");
        cfDef.setDefault_validation_class("IntegerType");
        ColumnDef columnDef = new ColumnDef(ByteBuffer.wrap("PERSON_NAME".getBytes()), "UTF8Type");
        columnDef.index_type = IndexType.KEYS;
        cfDef.addToColumn_metadata(columnDef);

        ColumnDef columnDef1 = new ColumnDef(ByteBuffer.wrap("ADDRESS_ID".getBytes()), "IntegerType");
        columnDef1.index_type = IndexType.KEYS;
        cfDef.addToColumn_metadata(columnDef1);

        List<CfDef> cfDefs = new ArrayList<CfDef>();
        cfDefs.add(cfDef);

        try
        {
            ksDef = CassandraCli.client.describe_keyspace("KunderaTests");
            CassandraCli.client.set_keyspace("KunderaTests");

            List<CfDef> cfDefn = ksDef.getCf_defs();

            // CassandraCli.client.set_keyspace("KunderaTests");
            for (CfDef cfDef1 : cfDefn)
            {

                if (cfDef1.getName().equalsIgnoreCase("PERSONNEL"))
                {

                    CassandraCli.client.system_drop_column_family("PERSONNEL");

                }
            }
            CassandraCli.client.system_add_column_family(cfDef);

        }
        catch (NotFoundException e)
        {

            addKeyspace(ksDef, cfDefs);
        }

        CassandraCli.client.set_keyspace("KunderaTests");

    }

    @Override
    protected void loadDataForHABITAT() throws TException, InvalidRequestException, UnavailableException,
            TimedOutException, SchemaDisagreementException
    {
        KsDef ksDef = null;
        CfDef cfDef2 = new CfDef();
        cfDef2.name = "ADDRESS";
        cfDef2.keyspace = "KunderaTests";
        cfDef2.setDefault_validation_class("UTF8Type");

        ColumnDef columnDef1 = new ColumnDef(ByteBuffer.wrap("STREET".getBytes()), "UTF8Type");
        columnDef1.index_type = IndexType.KEYS;
        cfDef2.addToColumn_metadata(columnDef1);

        List<CfDef> cfDefs = new ArrayList<CfDef>();
        cfDefs.add(cfDef2);

        try
        {
            ksDef = CassandraCli.client.describe_keyspace("KunderaTests");
            CassandraCli.client.set_keyspace("KunderaTests");
            List<CfDef> cfDefss = ksDef.getCf_defs();
            // CassandraCli.client.set_keyspace("KunderaTests");
            for (CfDef cfDef : cfDefss)
            {

                if (cfDef.getName().equalsIgnoreCase("ADDRESS"))
                {

                    CassandraCli.client.system_drop_column_family("ADDRESS");

                }
            }
            CassandraCli.client.system_add_column_family(cfDef2);
        }
        catch (NotFoundException e)
        {
            addKeyspace(ksDef, cfDefs);
        }
        CassandraCli.client.set_keyspace("KunderaTests");

    }
}
