rdfGraph = LOAD 'rdfGraph.csv' USING PigStorage() AS (sub:chararray, pred:chararray, obj:chararray);










--origData = LIMIT orig 10;
--count something:
rdfGrouped = GROUP rdfGraph ALL;
/*
creates: 
(all,{(s1,p1,o1),(s2,p2,o2),(s2,p3,o3),(s3,p4,o4)})
*/
rdfCount = FOREACH rdfGrouped GENERATE COUNT(rdfGraph);--don't really understand why we need a grouping. if just counts a bag right, why not the original origData bag





/**
 * CREATING A NETWORK OF S->O NODES (directed, unlabelled)
 */
unlabbelledGraph = FOREACH rdfGraph GENERATE sub, obj, 1; --just use a weight of 1 for now





/**
 * CREATING A NETWORK OF (SPO) NODES
 */
--what we want: link the tuples in our original bag to each other
--s1, p1, o2 --> s1, p2, o2

REGISTER lib/datafu.jar;
define UnorderedPairs datafu.pig.bags.UnorderedPairs();

subGroup = GROUP rdfGraph BY sub;
/*
creates: 
("sub1",{("sub1","pred1","obj1"),("sub1","pred1","obj2"),("sub1","pred3","obj3")})
("sub2",{("sub2","pred4","obj4"),("sub2","pred5","obj5"),("sub2","pred6","obj5")})

*/
subGroupBags = FOREACH subGroup GENERATE rdfGraph;
/*
creates: 
({("sub1","pred1","obj1"),("sub1","pred1","obj2"),("sub1","pred3","obj3")})
({("sub2","pred4","obj4"),("sub2","pred5","obj5"),("sub2","pred6","obj5")})

*/
spoSubjectPairs = FOREACH subGroupBags GENERATE UnorderedPairs(rdfGraph);
/*
creates: 
({(("sub1","pred1","obj1"),("sub1","pred1","obj2")),(("sub1","pred1","obj1"),("sub1","pred3","obj3")),(("sub1","pred1","obj2"),("sub1","pred3","obj3"))})
({(("sub2","pred4","obj4"),("sub2","pred5","obj5")),(("sub2","pred4","obj4"),("sub2","pred6","obj5")),(("sub2","pred5","obj5"),("sub2","pred6","obj5"))})
*/
spoSubjectGraph = foreach spoSubjectPairs generate FLATTEN($0);
/*
creates:
(("sub1","pred1","obj1"),("sub1","pred1","obj2"))
(("sub1","pred1","obj1"),("sub1","pred3","obj3"))
(("sub1","pred1","obj2"),("sub1","pred3","obj3"))
(("sub2","pred4","obj4"),("sub2","pred5","obj5"))
(("sub2","pred4","obj4"),("sub2","pred6","obj5"))
(("sub2","pred5","obj5"),("sub2","pred6","obj5"))
 */

predGroup = GROUP rdfGraph BY pred;
predGroupBags = FOREACH predGroup GENERATE rdfGraph;
predPredicatePairs = FOREACH predGroupBags GENERATE UnorderedPairs(rdfGraph);
spoPredicateGraph = foreach predPredicatePairs generate FLATTEN($0);

objGroup = GROUP rdfGraph BY obj;
objGroupBags = FOREACH objGroup GENERATE rdfGraph;
spoObjectPairs = FOREACH objGroupBags GENERATE UnorderedPairs(rdfGraph);
spoObjectGraph = foreach spoObjectPairs generate FLATTEN($0);


spoGraph = UNION spoSubjectGraph, spoPredicateGraph, spoObjectGraph;
/*
 creates
 (("sub1","pred1","obj1"),("sub1","pred1","obj2"))
 (("sub1","pred1","obj1"),("sub1","pred3","obj3"))
 (("sub1","pred1","obj2"),("sub1","pred1","obj1"))
 */
 
 /*
  * Make weighted graph. Append a numerical value to the tuples, representing how many sub/pred/obj both share
  * Requires:
  * 	Detecting (s1p1o1,s1p2o2) === (s1p2o2,s1p1o1)
  * example above should result in:
 (("sub1","pred1","obj1"),("sub1","pred1","obj2"), 2)
 (("sub1","pred1","obj1"),("sub1","pred3","obj3"), 1)
  */
--bla = FOREACH spoGraph {
--	part1 = CONCAT($0.sub, CONCAT($0.pred, $0.obj));
--	part2 = CONCAT($1.sub, CONCAT($1.pred, $1.obj));
--	--check if there exists something in spoGraph with the inverse
--	generate part1, part2;
--}
weightedSpoGraph = FOREACH spoGraph GENERATE $0, $1, 1; --just use a weight of 1 for each node for now... (do the thing above later)












