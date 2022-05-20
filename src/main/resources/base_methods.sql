/* SQL query template to find all methods a given method overrides (using only super-classes, no interfaces).
   Parameters $ClassName and $MethodName must be inserted using string replacement.
   The query parameters cannot be inserted using prepared statements, because H2 doesn't support prepared recursive
   queries.
 */

WITH RECURSIVE
    SubClassHierarchy(MethodId, MethodName, ClassId, ClassName, File, SuperClassId)
        AS (WITH MethodOwnership(MethodId, MethodName, File, ClassId, Classname)
                     AS (SELECT MTable.identifier As MethodId,
                                MTable.name       As MethodName,
                                MTable.file       As File,
                                CTable.identifier As ClassId,
                                CTable.name       As Classname
                         FROM ContentMappingSymbol AS MTable
                                  INNER JOIN ContentMappingSymbol As CTable
                                             ON MTable.type = 2 AND CTable.type = 1 AND MTable.file = CTable.file)
            SELECT CMS.MethodId, CMS.MethodName, CMS.ClassId, CMS.ClassName, CMS.File, CMR.symbol
            FROM MethodOwnership AS CMS
                     LEFT JOIN ContentMappingReference AS CMR
                               ON CMR.type = 6 AND CMR.file = CMS.file
            WHERE CMS.ClassName = $ClassName
              AND CMS.MethodName = $MethodName
            UNION ALL
            SELECT SubClassHierarchy.MethodId,
                   SubClassHierarchy.MethodName,
                   CMSN.identifier,
                   CMSN.name,
                   CMSN.file,
                   CMRN.symbol
            FROM SubClassHierarchy
                     LEFT JOIN ContentMappingSymbol AS CMSN
                               ON SubClassHierarchy.SuperClassId = CMSN.identifier
                     LEFT JOIN ContentMappingReference AS CMRN
                               ON CMRN.type = 6 AND CMSN.file = CMRN.file
            WHERE CMSN.file IS NOT NULL),
    MethodOwnership(MethodId, MethodName, File, ClassId, Classname) AS (SELECT MTable.identifier As MethodId,
                                                                               MTable.name       As MethodName,
                                                                               MTable.file       As File,
                                                                               CTable.identifier As ClassId,
                                                                               CTable.name       As Classname
                                                                        FROM ContentMappingSymbol AS MTable
                                                                                 INNER JOIN ContentMappingSymbol As CTable
                                                                                            ON MTable.type = 2 AND CTable.type = 1 AND MTable.file = CTable.file)
SELECT SH.MethodId As BaseMethodId, MO.MethodId As OverrideMethodId
FROM SubClassHierarchy AS SH
         INNER JOIN MethodOwnership AS MO
                    ON SH.MethodName = MO.MethodName AND SH.ClassId = MO.ClassId
WHERE SH.MethodId != MO.MethodId