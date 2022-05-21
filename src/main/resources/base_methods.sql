/* SQL query template to find all methods a given method overrides (using only super-classes, no interfaces).
   Parameters $ClassName and $MethodName are to be inserted using string replacement, as are the id's for
   $MethodSymbolType, $ClassSymbolType and $SuperTypeRef (the id's for the respective symbol and reference types)
   The query parameters cannot be inserted using prepared statements, because H2 doesn't support prepared recursive
   queries. The symbol/reference type IDs can theoretically be exchanged for subqueries
 */

WITH RECURSIVE
    MethodOwnership(MethodId, MethodName, File, ClassId, Classname)
        AS (SELECT MTable.identifier As MethodId,
                   MTable.name       As MethodName,
                   MTable.file       As File,
                   CTable.identifier As ClassId,
                   CTable.name       As Classname
            FROM ContentMappingSymbol AS MTable
                     INNER JOIN ContentMappingSymbol As CTable
                                ON MTable.type = $MethodSymbolType AND CTable.type = $ClassSymbolType
                AND MTable.file = CTable.file),
    SubClassHierarchy(MethodId, MethodName, ClassId, ClassName, File, SuperClassId)
        AS (SELECT CMS.MethodId, CMS.MethodName, CMS.ClassId, CMS.ClassName, CMS.File, CMR.symbol
            FROM MethodOwnership AS CMS
                     LEFT JOIN ContentMappingReference AS CMR
                               ON CMR.type = $SuperTypeRef AND CMR.file = CMS.file
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
                ON CMRN.type = $SuperTypeRef AND CMSN.file = CMRN.file
            WHERE CMSN.file IS NOT NULL)
SELECT SH.MethodId As BaseMethodId, MO.MethodId As OverrideMethodId
FROM SubClassHierarchy AS SH
         INNER JOIN MethodOwnership AS MO
                    ON SH.MethodName = MO.MethodName AND SH.ClassId = MO.ClassId
WHERE SH.MethodId != MO.MethodId