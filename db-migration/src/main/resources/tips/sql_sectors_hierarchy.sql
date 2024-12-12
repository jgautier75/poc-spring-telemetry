with recursive sectorHierarchy(id,code,label,sdepth,parent_id, path_info) as (
    select sector.id, sector.code as "sectorCode",sector.label as "sectorLabel", 1 as "sdepth",sector.parent_id, sector.label::text as path_info
    from sectors sector where sector.parent_id is null and sector.org_id=1
    union
    select child.id,child.code as "sectorCode", child.label as "sectorLabel",(sh.sdepth+1),child.parent_id , sh.path_info || '>' || child."label"
    from sectors child join sectorHierarchy sh on child.parent_id=sh.id
) select * from sectorHierarchy order by path_info asc, label asc;