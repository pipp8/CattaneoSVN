SELECT [dbProcesso].[dbo].[Ciclo].[IdCiclo]
      ,[dbProcesso].[dbo].[Ciclo].[StatoAttivo]
      ,[dbProcesso].[dbo].[Ciclo].[Descrizione]
      ,[dbProcesso].[dbo].[Ciclo].[Indirizzo]
      ,[dbProcesso].[dbo].[Ciclo].[Campionamento]
      ,[dbProcesso].[dbo].[Stato].[Descrizione]
      ,[dbProcesso].[dbo].[Stato].[IdStato]  
  FROM [dbProcesso].[dbo].[Ciclo], [dbProcesso].[dbo].[Stato]
  WHERE [dbProcesso].[dbo].[Ciclo].[IdCiclo] = [dbProcesso].[dbo].[Stato].[IdCiclo] AND
		[dbProcesso].[dbo].[Ciclo].[StatoAttivo] = [dbProcesso].[dbo].[Stato].[IdStato]
GO


