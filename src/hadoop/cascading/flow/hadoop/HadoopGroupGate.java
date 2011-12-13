/*
 * Copyright (c) 2007-2011 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.cascading.org/
 *
 * This file is part of the Cascading project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cascading.flow.hadoop;

import java.util.Iterator;

import cascading.flow.FlowProcess;
import cascading.flow.stream.Duct;
import cascading.flow.stream.GroupGate;
import cascading.flow.stream.StreamGraph;
import cascading.pipe.Group;
import cascading.pipe.cogroup.GroupByClosure;
import cascading.tuple.Tuple;
import org.apache.hadoop.mapred.OutputCollector;

/**
 *
 */
public abstract class HadoopGroupGate extends GroupGate
  {
  protected GroupByClosure closure;
  protected OutputCollector collector;

  public HadoopGroupGate( FlowProcess flowProcess, Group group, Role role )
    {
    super( flowProcess, group, role );
    }

  @Override
  public void bind( StreamGraph streamGraph )
    {
    allPrevious = getAllPreviousFor( streamGraph );

    if( role != Role.sink )
      next = getNextFor( streamGraph );
    }

  @Override
  public void prepare()
    {
    collector = ( (HadoopFlowProcess) flowProcess ).getOutputCollector();
    }

  @Override
  public void start( Duct previous )
    {
    if( next != null )
      super.start( previous );
    }

  @Override
  public void complete( Duct previous )
    {
    if( next != null )
      super.complete( previous );
    }

  public void run( Tuple key, Iterator values )
    {
    key = unwrapGrouping( key );

    closure.reset( key, values );

    values = group.getJoiner().getIterator( closure );

    groupingEntry.setTuple( key );
    tupleEntryIterator.reset( values );

    next.receive( this, grouping );
    }

  protected abstract Tuple unwrapGrouping( Tuple key );
  }